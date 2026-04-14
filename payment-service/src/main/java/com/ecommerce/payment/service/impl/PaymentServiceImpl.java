package com.ecommerce.payment.service.impl;

import com.ecommerce.commons.event.OrderPlacedEvent;
import com.ecommerce.commons.event.PaymentSuccessEvent;
import com.ecommerce.payment.utils.enums.PaymentStatus;
import com.ecommerce.commons.exception.ResourceNotFoundException;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.dtos.request.InitiatePaymentRequest;
import com.ecommerce.payment.entity.dtos.response.PaymentResponse;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${stripe.api.key:sk_test_dummy}")
    private String stripeApiKey;

    // Auto-initiate payment when order is placed
    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Order placed event received for order: {}", event.getOrderId());
        InitiatePaymentRequest request = new InitiatePaymentRequest();
        request.setOrderId(event.getOrderId());
        request.setUserId(event.getUserId());
        request.setAmount(event.getTotalAmount());
        request.setCurrency("INR");
        initiatePayment(request);
    }

    @Override
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .gateway("stripe")
                .status(PaymentStatus.PENDING)
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment initiated: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        return toResponse(paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId)));
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String signature) {
        log.info("Stripe webhook received, processing payment confirmation");
        String eventId = "stripe_" + UUID.randomUUID();
        UUID orderId = null;

        try {
            JsonNode root = objectMapper.readTree(payload);
            eventId = root.path("id").asText(eventId);
            if (root.hasNonNull("orderId")) {
                orderId = UUID.fromString(root.get("orderId").asText());
            } else {
                String metadataOrderId = root.path("data").path("object").path("metadata").path("orderId").asText(null);
                if (metadataOrderId != null && !metadataOrderId.isBlank()) {
                    orderId = UUID.fromString(metadataOrderId);
                }
            }
        } catch (Exception ex) {
            log.warn("Unable to parse webhook payload, using latest pending payment fallback: {}", ex.getMessage());
        }

        Payment payment = (orderId != null
                ? paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING)
                : paymentRepository.findFirstByStatusOrderByCreatedAtDesc(PaymentStatus.PENDING))
                .orElseThrow(() -> new ResourceNotFoundException("No pending payment found for webhook processing"));

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(eventId);
        payment.setPaidAt(LocalDateTime.now());
        Payment updated = paymentRepository.save(payment);
        publishPaymentSuccessEvent(updated);
    }

    private void publishPaymentSuccessEvent(Payment payment) {
        PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                .eventType("PAYMENT_SUCCESS")
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("payment-events", payment.getOrderId().toString(), event);
        log.info("PaymentSuccessEvent published for order: {}", payment.getOrderId());
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
