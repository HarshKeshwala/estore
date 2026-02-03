package com.estore.gateway.config;

import com.estore.gateway.filter.JwtAuthFilter;
import com.estore.gateway.handler.ProxyHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    private final ProxyHandler proxyHandler;
    private final JwtAuthFilter jwtAuthFilter;

    public RouterConfig(ProxyHandler proxyHandler, JwtAuthFilter jwtAuthFilter) {
        this.proxyHandler = proxyHandler;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
                // Auth routes - no JWT validation
                .POST("/api/auth/**", proxyHandler::proxyToUserService)
                .GET("/api/auth/**", proxyHandler::proxyToUserService)

                // User routes - JWT validation required
                .GET("/api/users/**", jwtAuthFilter.filter(proxyHandler::proxyToUserService))
                .PUT("/api/users/**", jwtAuthFilter.filter(proxyHandler::proxyToUserService))

                // Product routes - JWT validation required (except GET which is public)
                .GET("/api/products/**", proxyHandler::proxyToProductService)
                .POST("/api/products/**", jwtAuthFilter.filter(proxyHandler::proxyToProductService))
                .PUT("/api/products/**", jwtAuthFilter.filter(proxyHandler::proxyToProductService))
                .DELETE("/api/products/**", jwtAuthFilter.filter(proxyHandler::proxyToProductService))

                // Cart routes - JWT validation required
                .GET("/api/cart/**", jwtAuthFilter.filter(proxyHandler::proxyToOrderService))
                .POST("/api/cart/**", jwtAuthFilter.filter(proxyHandler::proxyToOrderService))
                .PUT("/api/cart/**", jwtAuthFilter.filter(proxyHandler::proxyToOrderService))
                .DELETE("/api/cart/**", jwtAuthFilter.filter(proxyHandler::proxyToOrderService))

                // Order routes - JWT validation required
                .GET("/api/orders/**", jwtAuthFilter.filter(proxyHandler::proxyToOrderService))
                .POST("/api/orders/**", jwtAuthFilter.filter(proxyHandler::proxyToOrderService))

                .build();
    }
}
