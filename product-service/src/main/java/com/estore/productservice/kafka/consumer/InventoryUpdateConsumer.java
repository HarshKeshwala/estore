package com.estore.productservice.kafka.consumer;

import com.estore.productservice.kafka.event.InventoryUpdateEvent;
import com.estore.productservice.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryUpdateConsumer.class);

    private final ProductService productService;

    public InventoryUpdateConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "${kafka.topics.inventory-update}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeInventoryUpdate(InventoryUpdateEvent event) {
        log.info("Received inventory update event: eventId={}, productId={}, quantityChange={}, orderId={}",
                event.eventId(), event.productId(), event.quantityChange(), event.orderId());

        try {
            productService.updateStock(event.productId(), event.quantityChange());
            log.info("Successfully updated stock for product {}", event.productId());
        } catch (ProductService.InsufficientStockException e) {
            log.error("Failed to update stock for product {}: {}", event.productId(), e.getMessage());
        }
    }
}
