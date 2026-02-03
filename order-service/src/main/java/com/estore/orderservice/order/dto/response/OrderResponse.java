package com.estore.orderservice.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt
) {}
