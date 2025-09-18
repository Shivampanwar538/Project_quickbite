package com.quickbite.controller;
import com.quickbite.dto.OrderDTO;
import com.quickbite.dto.OrderRequest;
import com.quickbite.model.MenuItem;
import com.quickbite.repository.menuRepository;
import org.springframework.web.bind.annotation.*;
import com.quickbite.repository.orderRepository;
import com.quickbite.model.Order;
import com.quickbite.model.User;
import com.quickbite.repository.userRepository;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final orderRepository orderRepo;
    private final menuRepository menuRepo;
    private final userRepository userRepo;
    private OrderRequest req;

    public OrderController(orderRepository orderRepo, menuRepository menuRepo, userRepository userRepo) {
        this.orderRepo = orderRepo;
        this.menuRepo = menuRepo;
        this.userRepo = userRepo;
    }
//    @GetMapping("/pending")
//    public List<Order> getOrders() { return orderRepo.findAll(); }



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


//    @GetMapping("/all")
//    public List<Order> getAllOrders() {
//        return orderRepo.findAll();
//    }


    @GetMapping("/user/{userId}")
    public List<OrderDTO> getOrdersByUser(@PathVariable Long userId) {
        return orderRepo.findByUserId(userId).stream()
                .map(OrderDTO::new)   // calls constructor OrderDTO(Order order)
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
            .map(o -> {
                Order order = (Order) o;
                return new OrderDTO(
                        order.getId(),
                        order.getMenuItem().getName(),
                        order.getStatus(),
                        order.getUser().getUsername()
                );
            })
            .toList();
}





    @PutMapping("/{id}/status")
    public OrderDTO updateStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepo.findById(id).orElseThrow();
        order.setStatus(status);
        return new OrderDTO(orderRepo.save(order));
    }

}
