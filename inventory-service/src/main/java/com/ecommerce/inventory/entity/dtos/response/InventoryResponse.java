package com.ecommerce.inventory.entity.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryResponse {
    private UUID id;
    private UUID productId;
    private int quantity;
    private int reserved;
    private String warehouse;
}

