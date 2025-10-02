package com.quickbite.controller;

import com.quickbite.dto.OrderDTO;
import com.quickbite.dto.OrderRequest;
import com.quickbite.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<OrderDTO> placeOrder(@Valid @RequestBody OrderRequest request) {
        OrderDTO placedOrder = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(placedOrder);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable String userId) {
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<OrderDTO>> getPendingOrders() {
        List<OrderDTO> pendingOrders = orderService.getPendingOrders();
        return ResponseEntity.ok(pendingOrders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateStatus(@PathVariable String id,
                                                 @RequestParam String status) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }
}