package com.estore.orderservice.kafka.producer;

import java.time.LocalDateTime;

public record InventoryUpdateEvent(
        String eventId,
        Long productId,
        int quantityChange,
        Long orderId,
        LocalDateTime timestamp
) {}
