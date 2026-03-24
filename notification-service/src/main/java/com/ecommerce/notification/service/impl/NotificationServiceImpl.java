package com.ecommerce.notification.service.impl;

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

    // ── private send methods — wire SendGrid / Twilio here ──────────────

    private void sendOrderConfirmationEmail(OrderPlacedEvent event) {
        // TODO: SendGrid integration
        // Mail mail = new Mail();
        // mail.setFrom(new Email("noreply@ecommerce.com"));
        // mail.setSubject("Order Confirmed #" + event.getOrderId());
        log.info("[EMAIL] Order confirmation sent for order: {}", event.getOrderId());
    }

    private void sendPaymentReceiptEmail(PaymentSuccessEvent event) {
        // TODO: SendGrid integration
        log.info("[EMAIL] Payment receipt sent for order: {}", event.getOrderId());
    }

    private void sendPaymentAlertEmail(PaymentFailedEvent event) {
        // TODO: SendGrid integration
        log.info("[EMAIL] Payment failure alert sent for order: {}", event.getOrderId());
    }

    private void sendDeliveryStatusSMS(DeliveryStatusEvent event) {
        // TODO: Twilio integration
        // Message.creator(new PhoneNumber(userPhone), new PhoneNumber(twilioNumber), msg).create();
        log.info("[SMS] Delivery status '{}' sent for order: {}", event.getStatus(), event.getOrderId());
    }
}
