package com.estore.orderservice.cart.service;

import com.estore.orderservice.cart.dto.request.AddCartItemRequest;
import com.estore.orderservice.cart.dto.request.UpdateCartItemRequest;
import com.estore.orderservice.cart.dto.response.CartItemResponse;
import com.estore.orderservice.cart.dto.response.CartResponse;
import com.estore.orderservice.cart.entity.Cart;
import com.estore.orderservice.cart.entity.CartItem;
import com.estore.orderservice.cart.repository.CartItemRepository;
import com.estore.orderservice.cart.repository.CartRepository;
import com.estore.orderservice.client.ProductResponse;
import com.estore.orderservice.client.ProductServiceClient;
import com.estore.orderservice.common.exception.CartItemNotFoundException;
import com.estore.orderservice.common.exception.InsufficientStockException;
import com.estore.orderservice.common.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductServiceClient productServiceClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productServiceClient = productServiceClient;
    }

    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        ProductResponse product = productServiceClient.getProduct(request.productId());
        if (product == null) {
            throw new ProductNotFoundException("Product not found: " + request.productId());
        }

        if (product.stockQuantity() < request.quantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + request.productId());
        }

        Cart cart = getOrCreateCart(userId);

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId())
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.quantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(request.productId());
            newItem.setQuantity(request.quantity());
            newItem.setPriceAtAddition(product.price());
            cart.addItem(newItem);
            cartRepository.save(cart);
        }

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));

        if (!productServiceClient.checkStock(item.getProductId(), request.quantity())) {
            throw new InsufficientStockException("Insufficient stock for product: " + item.getProductId());
        }

        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));

        cart.removeItem(item);
        cartItemRepository.delete(item);

        return toCartResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cart.clearItems();
            cartRepository.save(cart);
        }
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .toList();

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getUserId(), itemResponses, total);
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        ProductResponse product = productServiceClient.getProduct(item.getProductId());
        String productName = product != null ? product.name() : "Unknown Product";
        BigDecimal subtotal = item.getPriceAtAddition().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                productName,
                item.getQuantity(),
                item.getPriceAtAddition(),
                subtotal
        );
    }
}
