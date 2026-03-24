package com.ecommerce.commons.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentSuccessEvent {
    private String eventType;
    private UUID orderId;
    private UUID userId;
    private String transactionId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
}

