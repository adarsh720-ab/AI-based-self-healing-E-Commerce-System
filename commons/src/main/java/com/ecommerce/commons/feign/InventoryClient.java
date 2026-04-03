package com.ecommerce.commons.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "inventory-service",
        url = "${feign.inventory-service.url:http://localhost:8085}",
        fallback = InventoryClientFallback.class
)
public interface InventoryClient {

    @PatchMapping("/api/inventory/reserve")
    ReservationResponse reserveStock(@RequestBody ReservationRequest request);

    @PatchMapping("/api/inventory/release")
    void releaseStock(@RequestBody ReservationRequest request);

    @PatchMapping("/api/inventory/restock")
    void restockInventory(@RequestBody RestockRequest request);

    @GetMapping("/api/inventory/{productId}")
    InventoryResponse getInventory(@PathVariable UUID productId);

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    class ReservationRequest {
        private UUID productId;
        private int quantity;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    class ReservationResponse {
        private boolean success;
        private String message;
        private UUID productId;
        private int quantityReserved;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    class RestockRequest {
        private UUID productId;
        private int quantity;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    class InventoryResponse {
        private UUID id;
        private UUID productId;
        private int quantity;
        private int reserved;
        private String warehouse;
    }
}