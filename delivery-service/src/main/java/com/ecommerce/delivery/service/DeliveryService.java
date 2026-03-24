package com.ecommerce.delivery.service;

import com.ecommerce.delivery.entity.dtos.request.CreateDeliveryRequest;
import com.ecommerce.delivery.entity.dtos.response.DeliveryResponse;

import java.util.UUID;

public interface DeliveryService {
    DeliveryResponse createDelivery(CreateDeliveryRequest request);
    DeliveryResponse getDeliveryByOrderId(UUID orderId);
    DeliveryResponse updateDeliveryStatus(UUID deliveryId, String status);
}
