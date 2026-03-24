package com.ecommerce.product.service;

import com.ecommerce.product.entity.dtos.request.CreateProductRequest;
import com.ecommerce.product.entity.dtos.request.UpdateProductRequest;
import com.ecommerce.product.entity.dtos.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(UUID id, UpdateProductRequest request);
    void deleteProduct(UUID id);
    ProductResponse getProductById(UUID id);
    Page<ProductResponse> getAllProducts(Pageable pageable);
    Page<ProductResponse> searchProducts(String query, Pageable pageable);
}
