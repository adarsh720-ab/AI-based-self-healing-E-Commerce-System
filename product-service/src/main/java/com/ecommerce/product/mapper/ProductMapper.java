package com.ecommerce.product.mapper;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.dtos.request.CreateProductRequest;
import com.ecommerce.product.entity.dtos.response.ProductResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product createProductRequestToEntity(CreateProductRequest request);
    ProductResponse productToResponse(Product product);
}

