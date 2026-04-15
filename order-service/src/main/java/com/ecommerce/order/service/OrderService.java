package com.ecommerce.order.service;

import com.ecommerce.order.entity.dtos.request.CreateOrderRequest;
import com.ecommerce.order.entity.dtos.response.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrder(UUID orderId);
    List<OrderResponse> getUserOrders(UUID userId);
    OrderResponse cancelOrder(UUID orderId);
}