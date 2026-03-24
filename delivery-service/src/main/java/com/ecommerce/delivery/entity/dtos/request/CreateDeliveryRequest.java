package com.ecommerce.delivery.entity.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateDeliveryRequest {
    @NotNull
    private UUID orderId;
    
    @NotNull
    private UUID userId;
}


