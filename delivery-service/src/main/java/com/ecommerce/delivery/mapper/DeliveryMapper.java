package com.ecommerce.delivery.mapper;

import com.ecommerce.delivery.entity.Delivery;
import com.ecommerce.delivery.entity.dtos.response.DeliveryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {
    DeliveryResponse deliveryToResponse(Delivery delivery);
}
