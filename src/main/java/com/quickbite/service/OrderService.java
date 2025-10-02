package com.quickbite.service;

import com.quickbite.dto.OrderDTO;
import com.quickbite.dto.OrderRequest;
import com.quickbite.exception.OrderNotFoundException;
import com.quickbite.model.MenuItem;
import com.quickbite.model.Order;
import com.quickbite.model.User;
import com.quickbite.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final MenuService menuService;

    public OrderDTO placeOrder(OrderRequest request) {
        log.info("Placing order for user: {} and menu item: {}",
                request.getUserId(), request.getMenuItemId());

        User user = userService.findById(request.getUserId());
        MenuItem menuItem = menuService.findById(request.getMenuItemId());

        Order order = new Order();
        order.setUser(user);
        order.setMenuItem(menuItem);
        order.setItemName(menuItem.getName());
        order.setQuantity(request.getQuantity());
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with id: {}", savedOrder.getId());

        return new OrderDTO(savedOrder);
    }

    public List<OrderDTO> getOrdersByUserId(String userId) {
        log.info("Fetching orders for user: {}", userId);

        // Verify user exists
        userService.findById(userId);

        return orderRepository.findByUserId(userId).stream()
                .map(OrderDTO::new)
                .toList();
    }

    public List<OrderDTO> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(order -> new OrderDTO(
                        order.getId(),
                        order.getMenuItem().getName(),
                        order.getStatus(),
                        order.getUser().getUsername(),
                        order.getQuantity()
                ))
                .toList();
    }

    public List<OrderDTO> getPendingOrders() {
        log.info("Fetching pending orders");
        return orderRepository.findByStatus("PENDING").stream()
                .map(order -> new OrderDTO(
                        order.getId(),
                        order.getMenuItem().getName(),
                        order.getStatus(),
                        order.getUser().getUsername(),
                        order.getQuantity()
                ))
                .toList();
    }

    public OrderDTO updateOrderStatus(String orderId, String status) {
        log.info("Updating order {} status to: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Validate status
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        log.info("Order status updated successfully for order: {}", orderId);
        return new OrderDTO(savedOrder);
    }

    private boolean isValidStatus(String status) {
        return List.of("PENDING", "APPROVED", "REJECTED", "DELIVERED", "COMPLETED")
                .contains(status.toUpperCase());
    }

    public Order findById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}