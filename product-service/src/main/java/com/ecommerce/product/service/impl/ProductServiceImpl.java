package com.ecommerce.product.service.impl;

import com.ecommerce.commons.exception.ResourceNotFoundException;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.dtos.request.CreateProductRequest;
import com.ecommerce.product.entity.dtos.request.UpdateProductRequest;
import com.ecommerce.product.entity.dtos.response.ProductResponse;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository  productRepository;
    private final ProductMapper      productMapper;
    private final StringRedisTemplate redis;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = productMapper.createProductRequestToEntity(request);
        product.setActive(true);
        Product saved = productRepository.saveAndFlush(product);
        log.info("Product created: {}", saved.getId());
        return productMapper.productToResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setStockQuantity(request.getStockQuantity());

        Product updated = productRepository.save(product);
        redis.delete("product:" + id);
        log.info("Product updated: {}", id);
        return productMapper.productToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
        redis.delete("product:" + id);
        log.info("Product deleted: {}", id);
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        return productMapper.productToResponse(
                productRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id))
        );
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::productToResponse);
    }

    @Override
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        // Full Elasticsearch search — returns all for now, wire ES query here
        return productRepository.findAll(pageable).map(productMapper::productToResponse);
    }
}
