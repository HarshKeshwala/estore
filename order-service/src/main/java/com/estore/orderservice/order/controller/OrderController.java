package com.estore.orderservice.order.controller;

import com.estore.orderservice.order.dto.response.OrderResponse;
import com.estore.orderservice.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestHeader("X-User-Id") Long userId) {
        OrderResponse order = orderService.placeOrder(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrderHistory(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getOrderHistory(userId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(userId, orderId));
    }
}
