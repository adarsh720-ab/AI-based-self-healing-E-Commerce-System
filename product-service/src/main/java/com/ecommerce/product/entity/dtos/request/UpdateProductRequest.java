package com.ecommerce.product.entity.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateProductRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    private String category;
    private String imageUrl;

    @NotNull(message = "Stock quantity is required")
    @Min(0)
    private int stockQuantity;
}

