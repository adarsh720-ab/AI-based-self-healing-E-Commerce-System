package com.ecommerce.notification.service;

import com.ecommerce.commons.event.DeliveryStatusEvent;
import com.ecommerce.commons.event.OrderPlacedEvent;
import com.ecommerce.commons.event.PaymentFailedEvent;
import com.ecommerce.commons.event.PaymentSuccessEvent;

public interface NotificationService {
    void handleOrderPlaced(OrderPlacedEvent event);
    void handlePaymentSuccess(PaymentSuccessEvent event);
    void handlePaymentFailed(PaymentFailedEvent event);
    void handleDeliveryStatusUpdate(DeliveryStatusEvent event);
}
