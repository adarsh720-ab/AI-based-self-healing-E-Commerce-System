package com.ecommerce.commons.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderPlacedEvent {
    private String eventType;
    private UUID orderId;
    private UUID userId;
    private UUID addressId;
    private BigDecimal totalAmount;
    private List<ItemDto> items;
    private LocalDateTime timestamp;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ItemDto {
        private UUID productId;
        private int quantity;
        private BigDecimal unitPrice;
    }
}

