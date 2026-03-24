package com.ecommerce.commons.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentFailedEvent {
    private String eventType;
    private UUID orderId;
    private UUID userId;
    private String reason;
    private LocalDateTime timestamp;
}

