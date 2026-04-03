package com.ecommerce.commons.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class InventoryClientFallback implements InventoryClient {

    @Override
    public ReservationResponse reserveStock(ReservationRequest request) {
        log.error("CIRCUIT OPEN: inventory-service unavailable — reserveStock failed for product {}",
                request.getProductId());
        return ReservationResponse.builder()
                .success(false)
                .message("Inventory service temporarily unavailable. Please retry.")
                .productId(request.getProductId())
                .quantityReserved(0)
                .build();
    }

    @Override
    public void releaseStock(ReservationRequest request) {
        log.error("CIRCUIT OPEN: inventory-service unavailable — releaseStock failed for product {}",
                request.getProductId());
    }

    @Override
    public void restockInventory(RestockRequest request) {
        log.error("CIRCUIT OPEN: inventory-service unavailable — restock failed for product {}",
                request.getProductId());
    }

    @Override
    public InventoryResponse getInventory(UUID productId) {
        log.error("CIRCUIT OPEN: inventory-service unavailable — getInventory failed for {}",
                productId);
        return InventoryResponse.builder()
                .productId(productId)
                .quantity(0)
                .reserved(0)
                .warehouse("UNAVAILABLE")
                .build();
    }
}