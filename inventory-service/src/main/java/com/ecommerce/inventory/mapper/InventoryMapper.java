package com.ecommerce.inventory.mapper;

import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.dtos.response.InventoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    InventoryResponse inventoryToResponse(Inventory inventory);
}
