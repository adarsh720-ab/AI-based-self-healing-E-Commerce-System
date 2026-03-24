package com.ecommerce.inventory.service;

import com.ecommerce.inventory.entity.dtos.request.ReservationRequest;
import com.ecommerce.inventory.entity.dtos.request.RestockRequest;
import com.ecommerce.inventory.entity.dtos.response.InventoryResponse;
import com.ecommerce.inventory.entity.dtos.response.ReservationResponse;

import java.util.UUID;

public interface InventoryService {
    InventoryResponse getInventory(UUID productId);
    ReservationResponse reserveStock(ReservationRequest request);
    void releaseStock(ReservationRequest request);
    void restockInventory(RestockRequest request);
}
