package com.estore.orderservice.kafka.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class InventoryUpdateProducer {

    private static final Logger log = LoggerFactory.getLogger(InventoryUpdateProducer.class);

    private final KafkaTemplate<String, InventoryUpdateEvent> kafkaTemplate;
    private final String topic;

    public InventoryUpdateProducer(KafkaTemplate<String, InventoryUpdateEvent> kafkaTemplate,
                                   @Value("${kafka.topics.inventory-update}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendInventoryUpdate(Long productId, int quantityChange, Long orderId) {
        InventoryUpdateEvent event = new InventoryUpdateEvent(
                UUID.randomUUID().toString(),
                productId,
                quantityChange,
                orderId,
                LocalDateTime.now()
        );

        log.info("Sending inventory update event: productId={}, quantityChange={}, orderId={}",
                productId, quantityChange, orderId);

        kafkaTemplate.send(topic, productId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent inventory update event for product {}", productId);
                    } else {
                        log.error("Failed to send inventory update event for product {}", productId, ex);
                    }
                });
    }
}
