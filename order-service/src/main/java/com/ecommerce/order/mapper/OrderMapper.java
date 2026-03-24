package com.ecommerce.order.mapper;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.dtos.response.OrderResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse orderToResponse(Order order);
}
