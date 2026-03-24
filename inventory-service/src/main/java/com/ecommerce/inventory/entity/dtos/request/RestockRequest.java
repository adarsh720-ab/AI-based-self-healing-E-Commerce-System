package com.ecommerce.inventory.entity.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RestockRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Quantity is required")
    @Min(1)
    private int quantity;
}

