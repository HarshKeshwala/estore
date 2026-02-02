package com.estore.orderservice.order.service;

import com.estore.orderservice.cart.entity.Cart;
import com.estore.orderservice.cart.entity.CartItem;
import com.estore.orderservice.cart.repository.CartRepository;
import com.estore.orderservice.client.ProductResponse;
import com.estore.orderservice.client.ProductServiceClient;
import com.estore.orderservice.common.exception.EmptyCartException;
import com.estore.orderservice.common.exception.InsufficientStockException;
import com.estore.orderservice.common.exception.OrderNotFoundException;
import com.estore.orderservice.kafka.producer.InventoryUpdateProducer;
import com.estore.orderservice.order.dto.response.OrderItemResponse;
import com.estore.orderservice.order.dto.response.OrderResponse;
import com.estore.orderservice.order.entity.Order;
import com.estore.orderservice.order.entity.OrderItem;
import com.estore.orderservice.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final InventoryUpdateProducer inventoryUpdateProducer;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductServiceClient productServiceClient,
                        InventoryUpdateProducer inventoryUpdateProducer) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productServiceClient = productServiceClient;
        this.inventoryUpdateProducer = inventoryUpdateProducer;
    }

    @Transactional
    public OrderResponse placeOrder(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EmptyCartException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cart is empty");
        }

        // Validate stock for all items
        for (CartItem cartItem : cart.getItems()) {
            if (!productServiceClient.checkStock(cartItem.getProductId(), cartItem.getQuantity())) {
                throw new InsufficientStockException("Insufficient stock for product: " + cartItem.getProductId());
            }
        }

        // Create order
        Order order = new Order();
        order.setUserId(userId);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            ProductResponse product = productServiceClient.getProduct(cartItem.getProductId());

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(product != null ? product.name() : "Unknown");
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPriceAtAddition());

            order.addItem(orderItem);

            BigDecimal itemTotal = cartItem.getPriceAtAddition()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Send inventory update events for each item
        for (OrderItem orderItem : savedOrder.getItems()) {
            inventoryUpdateProducer.sendInventoryUpdate(
                    orderItem.getProductId(),
                    -orderItem.getQuantity(),  // negative to decrease stock
                    savedOrder.getId()
            );
        }

        // Clear the cart
        cart.clearItems();
        cartRepository.save(cart);

        return toOrderResponse(savedOrder);
    }

    public List<OrderResponse> getOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    public OrderResponse getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::toOrderItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                itemResponses,
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice(),
                subtotal
        );
    }
}
