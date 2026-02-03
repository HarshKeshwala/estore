package com.estore.productservice.product.service;

import com.estore.productservice.product.dto.request.ProductRequest;
import com.estore.productservice.product.dto.response.ProductResponse;
import com.estore.productservice.product.entity.Product;
import com.estore.productservice.product.mapper.ProductMapper;
import com.estore.productservice.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        productMapper.updateEntity(request, product);
        Product updated = productRepository.save(product);
        return productMapper.toResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public void updateStock(Long productId, int quantityChange) {
        int updated = productRepository.updateStock(productId, quantityChange);
        if (updated == 0) {
            throw new InsufficientStockException("Insufficient stock for product: " + productId);
        }
    }

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) {
            super(message);
        }
    }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}
