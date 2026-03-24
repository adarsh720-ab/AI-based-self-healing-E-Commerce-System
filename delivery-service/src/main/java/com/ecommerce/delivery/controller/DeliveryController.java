package com.ecommerce.delivery.controller;

import com.ecommerce.delivery.entity.dtos.request.UpdateDeliveryStatusRequest;
import com.ecommerce.delivery.entity.dtos.response.DeliveryResponse;
import com.ecommerce.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(deliveryService.getDeliveryByOrderId(orderId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(id, request.getStatus()));
    }
}
