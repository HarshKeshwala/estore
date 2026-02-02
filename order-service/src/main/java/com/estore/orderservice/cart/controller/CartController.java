package com.estore.orderservice.cart.controller;

import com.estore.orderservice.cart.dto.request.AddCartItemRequest;
import com.estore.orderservice.cart.dto.request.UpdateCartItemRequest;
import com.estore.orderservice.cart.dto.response.CartResponse;
import com.estore.orderservice.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddCartItemRequest request) {
        CartResponse cart = cartService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(userId, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
