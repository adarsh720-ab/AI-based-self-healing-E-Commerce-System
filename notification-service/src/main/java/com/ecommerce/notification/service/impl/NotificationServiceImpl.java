package com.ecommerce.notification.service.impl;

import com.ecommerce.commons.event.AnomalyEvent;
import com.ecommerce.commons.event.DeliveryStatusEvent;
import com.ecommerce.commons.event.OrderPlacedEvent;
import com.ecommerce.commons.event.PaymentFailedEvent;
import com.ecommerce.commons.event.PaymentSuccessEvent;
import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("ORDER_PLACED — sending confirmation email to user: {}", event.getUserId());
        sendOrderConfirmationEmail(event);
    }

    @Override
    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("PAYMENT_SUCCESS — sending receipt email for order: {}", event.getOrderId());
        sendPaymentReceiptEmail(event);
    }

    @Override
    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("PAYMENT_FAILED — sending alert email for order: {}", event.getOrderId());
        sendPaymentAlertEmail(event);
    }

    @Override
    @KafkaListener(topics = "delivery-events", groupId = "notification-service-group")
    public void handleDeliveryStatusUpdate(DeliveryStatusEvent event) {
        log.info("DELIVERY_UPDATED — sending SMS for order: {}", event.getOrderId());
        sendDeliveryStatusSMS(event);
    }

    @Override
    @KafkaListener(
            topics = "anomaly-events",
            groupId = "notification-service-group",
            containerFactory = "anomalyKafkaListenerContainerFactory"
    )
    public void handleAnomaly(AnomalyEvent event) {
        log.warn("🚨 ANOMALY RECEIVED — service={} type={} confidence={} score={}",
                event.getServiceId(),
                event.getAnomalyType(),
                event.getConfidence(),
                event.getAnomalyScore());

        // Route to correct handler based on anomaly type
        switch (event.getAnomalyType() != null ? event.getAnomalyType() : "unknown") {
            case "service_down"              -> handleServiceDown(event);
            case "latency_spike"             -> handleLatencySpike(event);
            case "latency_spike_with_errors" -> handleLatencySpike(event);
            case "error_spike"               -> handleErrorSpike(event);
            default                          -> handleGenericAnomaly(event);
        }
    }

    // ── Anomaly handlers ────────────────────────────────────────────────────

    private void handleServiceDown(AnomalyEvent event) {
        log.error("🔴 SERVICE DOWN | service={} | confidence={} | trace={}",
                event.getServiceId(), event.getConfidence(), event.getTraceId());
        // TODO: SendGrid — send CRITICAL alert email to engineering team
        // TODO: PagerDuty / OpsGenie integration for HIGH confidence
        log.info("[ALERT] Service down notification sent for: {}", event.getServiceId());
    }

    private void handleLatencySpike(AnomalyEvent event) {
        log.warn("🟡 LATENCY SPIKE | service={} | latencyMs={} | confidence={}",
                event.getServiceId(), event.getLatencyMs(), event.getConfidence());
        // TODO: SendGrid — send WARNING email to engineering team
        log.info("[ALERT] Latency spike notification sent for: {}", event.getServiceId());
    }

    private void handleErrorSpike(AnomalyEvent event) {
        log.warn("🟠 ERROR SPIKE | service={} | errorCode={} | confidence={}",
                event.getServiceId(), event.getErrorCode(), event.getConfidence());
        // TODO: SendGrid — send WARNING email to engineering team
        log.info("[ALERT] Error spike notification sent for: {}", event.getServiceId());
    }

    private void handleGenericAnomaly(AnomalyEvent event) {
        log.warn("⚪ ANOMALY | service={} | type={} | confidence={}",
                event.getServiceId(), event.getAnomalyType(), event.getConfidence());
        log.info("[ALERT] Generic anomaly notification sent for: {}", event.getServiceId());
    }

    // ── private send methods — wire SendGrid / Twilio here ──────────────────

    private void sendOrderConfirmationEmail(OrderPlacedEvent event) {
        log.info("[EMAIL] Order confirmation sent for order: {}", event.getOrderId());
    }

    private void sendPaymentReceiptEmail(PaymentSuccessEvent event) {
        log.info("[EMAIL] Payment receipt sent for order: {}", event.getOrderId());
    }

    private void sendPaymentAlertEmail(PaymentFailedEvent event) {
        log.info("[EMAIL] Payment failure alert sent for order: {}", event.getOrderId());
    }

    private void sendDeliveryStatusSMS(DeliveryStatusEvent event) {
        log.info("[SMS] Delivery status '{}' sent for order: {}", event.getStatus(), event.getOrderId());
    }
}