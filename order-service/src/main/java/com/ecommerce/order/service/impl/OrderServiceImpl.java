package com.ecommerce.order.service.impl;

import com.ecommerce.commons.event.OrderPlacedEvent;
import com.ecommerce.commons.event.PaymentSuccessEvent;
import com.ecommerce.commons.exception.ResourceNotFoundException;
import com.ecommerce.commons.feign.InventoryClient;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.dtos.request.CreateOrderRequest;
import com.ecommerce.order.entity.dtos.request.OrderItemRequest;
import com.ecommerce.order.entity.dtos.response.OrderItemResponse;
import com.ecommerce.order.entity.dtos.response.OrderResponse;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.utils.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                log.info("Order confirmed after payment success: {}", order.getId());
            }
        });
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Reserve stock for each item
        for (OrderItemRequest item : request.getItems()) {
            inventoryClient.reserveStock(
                    new InventoryClient.ReservationRequest(item.getProductId(), item.getQuantity()));
        }

        BigDecimal calculatedTotal = request.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.getTotalAmount() != null && request.getTotalAmount().compareTo(calculatedTotal) != 0) {
            log.warn("Ignoring client totalAmount {} for order; calculated total is {}",
                    request.getTotalAmount(), calculatedTotal);
        }

        Order order = Order.builder()
                .userId(request.getUserId())
                .addressId(request.getAddressId())
                .status(OrderStatus.PENDING)
                .totalAmount(calculatedTotal)
                .build();

        Order saved = orderRepository.saveAndFlush(order);
        final Order persistedOrder = saved;

        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .order(persistedOrder)
                        .productId(itemReq.getProductId())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        saved.setItems(items);
        saved = orderRepository.saveAndFlush(saved);

        Order responseOrder = orderRepository.findById(saved.getId()).orElse(saved);

        publishOrderPlacedEvent(responseOrder, request);
        log.info("Order created: {}", responseOrder.getId());
        return toResponse(responseOrder);
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
                .totalAmount(order.getTotalAmount())
                .items(request.getItems().stream()
                        .map(i -> new OrderPlacedEvent.ItemDto(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                        .collect(Collectors.toList()))
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("order-events", order.getId().toString(), event);
    }

    private OrderResponse toResponse(Order order) {
        LocalDateTime createdAt = order.getCreatedAt() != null
                ? order.getCreatedAt()
                : (order.getUpdatedAt() != null ? order.getUpdatedAt() : LocalDateTime.now());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .addressId(order.getAddressId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(createdAt)
                .items(order.getItems() == null ? List.of() : order.getItems().stream()
                        .map(i -> OrderItemResponse.builder()
                                .productId(i.getProductId())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
