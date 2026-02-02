package com.estore.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(RestClient.Builder builder,
                                @Value("${services.product.url}") String productServiceUrl) {
        this.restClient = builder.baseUrl(productServiceUrl).build();
    }

    public ProductResponse getProduct(Long productId) {
        return restClient.get()
                .uri("/products/{id}", productId)
                .retrieve()
                .body(ProductResponse.class);
    }

    public boolean checkStock(Long productId, int quantity) {
        ProductResponse product = getProduct(productId);
        return product != null && product.stockQuantity() >= quantity;
    }
}
