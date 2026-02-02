package com.estore.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    @Value("${services.user.url}")
    private String userServiceUrl;

    @Value("${services.product.url}")
    private String productServiceUrl;

    @Value("${services.order.url}")
    private String orderServiceUrl;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    public String getUserServiceUrl() {
        return userServiceUrl;
    }

    public String getProductServiceUrl() {
        return productServiceUrl;
    }

    public String getOrderServiceUrl() {
        return orderServiceUrl;
    }
}
