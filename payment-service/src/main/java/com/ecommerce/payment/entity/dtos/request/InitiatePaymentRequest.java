package com.ecommerce.payment.entity.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InitiatePaymentRequest {
    @NotNull
    private UUID orderId;
    
    @NotNull
    private UUID userId;
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    
    @NotBlank
    private String currency;
}

