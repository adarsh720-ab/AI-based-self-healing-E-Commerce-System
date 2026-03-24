package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.entity.dtos.request.ReservationRequest;
import com.ecommerce.inventory.entity.dtos.request.RestockRequest;
import com.ecommerce.inventory.entity.dtos.response.InventoryResponse;
import com.ecommerce.inventory.entity.dtos.response.ReservationResponse;
import com.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @PatchMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveStock(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(inventoryService.reserveStock(request));
    }

    @PatchMapping("/release")
    public ResponseEntity<Void> releaseStock(@Valid @RequestBody ReservationRequest request) {
        inventoryService.releaseStock(request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/restock")
    public ResponseEntity<Void> restockInventory(@Valid @RequestBody RestockRequest request) {
        inventoryService.restockInventory(request);
        return ResponseEntity.noContent().build();
    }
}
