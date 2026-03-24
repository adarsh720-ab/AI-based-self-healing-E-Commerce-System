package com.ecommerce.delivery.service.impl;

import com.ecommerce.commons.event.DeliveryStatusEvent;
import com.ecommerce.commons.event.PaymentSuccessEvent;
import com.ecommerce.commons.exception.ResourceNotFoundException;
import com.ecommerce.delivery.entity.Delivery;
import com.ecommerce.delivery.entity.dtos.request.CreateDeliveryRequest;
import com.ecommerce.delivery.entity.dtos.response.DeliveryResponse;
import com.ecommerce.delivery.repository.DeliveryRepository;
import com.ecommerce.delivery.service.DeliveryService;
import com.ecommerce.delivery.utils.enums.DeliveryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final KafkaTemplate<String, DeliveryStatusEvent> kafkaTemplate;

    // ── Kafka consumer ────────────────────────────────────────────────────────
    // Called automatically when payment-service publishes PAYMENT_SUCCESS event.
    // @KafkaListener is used here intentionally — it is NOT unused, it is triggered
    // by Kafka infrastructure, not by direct Java calls.
    @KafkaListener(topics = "payment-events", groupId = "delivery-service-group")
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Payment success received. Creating delivery for order: {}", event.getOrderId());
        CreateDeliveryRequest request = new CreateDeliveryRequest();
        request.setOrderId(event.getOrderId());
        request.setUserId(event.getUserId());
        createDelivery(request);
    }

    // ── Service interface methods ─────────────────────────────────────────────

    @Override
    @Transactional
    public DeliveryResponse createDelivery(CreateDeliveryRequest request) {
        Delivery delivery = Delivery.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .status(DeliveryStatus.PENDING)
                .trackingCode("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .estimatedDate(LocalDateTime.now().plusDays(5))
                .build();

        Delivery saved = deliveryRepository.save(delivery);
        log.info("Delivery created: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public DeliveryResponse getDeliveryByOrderId(UUID orderId) {
        return toResponse(
                deliveryRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Delivery not found for order: " + orderId))
        );
    }

    @Override
    @Transactional
    public DeliveryResponse updateDeliveryStatus(UUID deliveryId, String status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery not found: " + deliveryId));

        // DeliveryStatus is imported directly so no need for Delivery.DeliveryStatus prefix
        DeliveryStatus newStatus = DeliveryStatus.valueOf(status.toUpperCase());
        delivery.setStatus(newStatus);

        if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
        }

        Delivery updated = deliveryRepository.save(delivery);
        publishDeliveryStatusEvent(updated);
        log.info("Delivery status updated: {} -> {}", deliveryId, status);
        return toResponse(updated);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void publishDeliveryStatusEvent(Delivery delivery) {
        DeliveryStatusEvent event = DeliveryStatusEvent.builder()
                .eventType("DELIVERY_UPDATED")
                .orderId(delivery.getOrderId())
                .userId(delivery.getUserId())
                .trackingCode(delivery.getTrackingCode())
                .status(delivery.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("delivery-events", delivery.getOrderId().toString(), event);
    }

    private DeliveryResponse toResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .userId(delivery.getUserId())
                .trackingCode(delivery.getTrackingCode())
                .status(delivery.getStatus().name())
                .estimatedDate(delivery.getEstimatedDate())
                .deliveredAt(delivery.getDeliveredAt())
                .build();
    }
}