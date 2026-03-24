package com.ecommerce.payment.entity.dtos.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private UUID          id;
    private UUID          orderId;
    private UUID          userId;
    private String        status;
    private BigDecimal    amount;
    private String        currency;
    private String        transactionId;
    private LocalDateTime paidAt;
}

