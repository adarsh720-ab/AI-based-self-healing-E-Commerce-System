package com.ecommerce.order.service.impl;

import com.ecommerce.commons.event.OrderPlacedEvent;
import com.ecommerce.commons.exception.ResourceNotFoundException;
import com.ecommerce.commons.feign.InventoryClient;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.dtos.request.CreateOrderRequest;
import com.ecommerce.order.entity.dtos.request.OrderItemRequest;
import com.ecommerce.order.entity.dtos.response.OrderResponse;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.utils.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Reserve stock for each item
        for (OrderItemRequest item : request.getItems()) {
            inventoryClient.reserveStock(
                    new InventoryClient.ReservationRequest(item.getProductId(), item.getQuantity()));
        }

        Order order = Order.builder()
                .userId(request.getUserId())
                .addressId(request.getAddressId())
                .status(OrderStatus.PENDING)
                .totalAmount(request.getTotalAmount())
                .build();

        Order saved = orderRepository.save(order);

        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .order(saved)
                        .productId(itemReq.getProductId())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        saved.setItems(items);
        orderRepository.save(saved);

        publishOrderPlacedEvent(saved, request);
        log.info("Order created: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public OrderResponse getOrder(UUID orderId) {
        return toResponse(orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId)));
    }

    @Override
    public List<OrderResponse> getUserOrders(UUID userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        order.setStatus(OrderStatus.CANCELLED);

        for (OrderItem item : order.getItems()) {
            inventoryClient.releaseStock(
                    new InventoryClient.ReservationRequest(item.getProductId(), item.getQuantity()));
        }

        Order saved = orderRepository.save(order);
        log.info("Order cancelled: {}", orderId);
        return toResponse(saved);
    }

    private void publishOrderPlacedEvent(Order order, CreateOrderRequest request) {
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .eventType("ORDER_PLACED")
                .orderId(order.getId())
                .userId(order.getUserId())
                .addressId(order.getAddressId())
                .totalAmount(request.getTotalAmount())
                .items(request.getItems().stream()
                        .map(i -> new OrderPlacedEvent.ItemDto(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                        .collect(Collectors.toList()))
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("order-events", order.getId().toString(), event);
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .addressId(order.getAddressId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
