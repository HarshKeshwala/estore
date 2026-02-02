package com.estore.orderservice.cart.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal priceAtAddition,
        BigDecimal subtotal
) {}
