package com.ecommerce.notification.service;

import com.ecommerce.commons.event.*;

public interface NotificationService {
    void handleOrderPlaced(OrderPlacedEvent event);
    void handlePaymentSuccess(PaymentSuccessEvent event);
    void handlePaymentFailed(PaymentFailedEvent event);
    void handleDeliveryStatusUpdate(DeliveryStatusEvent event);
    void handleAnomaly(AnomalyEvent event);
}