package com.quickbite.controller;

import com.quickbite.dto.OrderDTO;
import com.quickbite.dto.OrderRequest;
import com.quickbite.model.MenuItem;
import com.quickbite.repository.MenuRepository;
import org.springframework.web.bind.annotation.*;
import com.quickbite.repository.OrderRepository;
import com.quickbite.model.Order;
import com.quickbite.model.User;
import com.quickbite.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/order")
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderRepository orderRepo;
    private final MenuRepository menuRepo;
    private final UserRepository userRepo;

    public OrderController(OrderRepository orderRepo, MenuRepository menuRepo, UserRepository userRepo) {
        this.orderRepo = orderRepo;
        this.menuRepo = menuRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/place")
    public OrderDTO placeOrder(@RequestBody OrderRequest request) {
        User user = userRepo.findById(request.getUserId()).orElseThrow();
        MenuItem item = menuRepo.findById(request.getMenuItemId()).orElseThrow();

        Order order = new Order();
        order.setUser(user);
        order.setMenuItem(item);
        order.setItemName(item.getName());
        order.setQuantity(request.getQuantity());
        order.setStatus("PENDING");

        return new OrderDTO(orderRepo.save(order));
    }

    @GetMapping("/user/{userId}")
    public List<OrderDTO> getOrdersByUser(@PathVariable String userId) {
        return orderRepo.findByUserId(userId).stream()
                .map(OrderDTO::new)
                .toList();
    }

    @GetMapping("/all")
    public List<OrderDTO> getAllOrders() {
        return orderRepo.findAll().stream()
                .map(o -> new OrderDTO(
                        o.getId(),
                        o.getMenuItem().getName(),
                        o.getStatus(),
                        o.getUser().getUsername()
                ))
                .toList();
    }

    @GetMapping("/pending")
    public List<OrderDTO> getPendingOrders() {
        return orderRepo.findByStatus("PENDING").stream()
                .map(order -> new OrderDTO(
                        order.getId(),
                        order.getMenuItem().getName(),
                        order.getStatus(),
                        order.getUser().getUsername()
                ))
                .toList();
    }

    @PutMapping("/{id}/status")
    public OrderDTO updateStatus(@PathVariable String id, @RequestParam String status) {
        Order order = orderRepo.findById(id).orElseThrow();
        order.setStatus(status);
        return new OrderDTO(orderRepo.save(order));
    }
}