package com.ecommerce.inventory.entity.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReservationResponse {
    private boolean success;
    private String message;
    private UUID productId;
    private int quantityReserved;
}

