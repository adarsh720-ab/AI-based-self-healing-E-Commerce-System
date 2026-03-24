package com.ecommerce.inventory.service.impl;

import com.ecommerce.commons.exception.InsufficientStockException;
import com.ecommerce.commons.exception.ResourceNotFoundException;
import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.dtos.request.ReservationRequest;
import com.ecommerce.inventory.entity.dtos.request.RestockRequest;
import com.ecommerce.inventory.entity.dtos.response.InventoryResponse;
import com.ecommerce.inventory.entity.dtos.response.ReservationResponse;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StringRedisTemplate  redis;

    @Override
    public InventoryResponse getInventory(UUID productId) {
        Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));

        return InventoryResponse.builder()
                .id(inv.getId())
                .productId(inv.getProductId())
                .quantity(inv.getQuantity())
                .reserved(inv.getReserved())
                .warehouse(inv.getWarehouse())
                .build();
    }

    @Override
    @Transactional
    public ReservationResponse reserveStock(ReservationRequest request) {
        Inventory inv = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + request.getProductId()));

        int available = inv.getQuantity() - inv.getReserved();
        if (available < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + available + ", Requested: " + request.getQuantity());
        }

        inv.setReserved(inv.getReserved() + request.getQuantity());
        inventoryRepository.save(inv);

        redis.opsForValue().set("stock:" + request.getProductId(),
                String.valueOf(available - request.getQuantity()), 30, TimeUnit.SECONDS);

        log.info("Stock reserved for product {}: {} units", request.getProductId(), request.getQuantity());

        return ReservationResponse.builder()
                .success(true)
                .message("Stock reserved successfully")
                .productId(request.getProductId())
                .quantityReserved(request.getQuantity())
                .build();
    }

    @Override
    @Transactional
    public void releaseStock(ReservationRequest request) {
        Inventory inv = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + request.getProductId()));

        inv.setReserved(Math.max(0, inv.getReserved() - request.getQuantity()));
        inventoryRepository.save(inv);
        redis.delete("stock:" + request.getProductId());
        log.info("Stock released for product {}: {} units", request.getProductId(), request.getQuantity());
    }

    @Override
    @Transactional
    public void restockInventory(RestockRequest request) {
        Inventory inv = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + request.getProductId()));

        inv.setQuantity(inv.getQuantity() + request.getQuantity());
        inventoryRepository.save(inv);
        redis.delete("stock:" + request.getProductId());
        log.info("Inventory restocked for product {}: {} units", request.getProductId(), request.getQuantity());
    }
}
