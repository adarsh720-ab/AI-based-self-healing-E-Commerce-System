package com.ecommerce.delivery.entity.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateDeliveryStatusRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|SHIPPED|OUT_FOR_DELIVERY|DELIVERED",
             message = "Status must be one of: PENDING, SHIPPED, OUT_FOR_DELIVERY, DELIVERED")
    private String status;
}
