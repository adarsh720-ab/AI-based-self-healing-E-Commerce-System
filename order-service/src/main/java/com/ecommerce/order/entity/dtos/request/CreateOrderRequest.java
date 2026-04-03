package com.ecommerce.order.entity.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateOrderRequest {
    private UUID userId;
    
    @NotNull
    private UUID addressId;
    
    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
    
    private BigDecimal totalAmount;
}

