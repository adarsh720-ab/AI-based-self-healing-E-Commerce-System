package com.ecommerce.order.entity.dtos.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItemRequest {
    @NotNull
    private UUID productId;
    @NotNull
    @Min(1)
    private int quantity;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal unitPrice;
}
