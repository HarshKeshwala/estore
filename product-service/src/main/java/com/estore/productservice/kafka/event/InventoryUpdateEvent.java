package com.estore.productservice.kafka.event;

import java.time.LocalDateTime;

public record InventoryUpdateEvent(
        String eventId,
        Long productId,
        int quantityChange,
        Long orderId,
        LocalDateTime timestamp
) {}
