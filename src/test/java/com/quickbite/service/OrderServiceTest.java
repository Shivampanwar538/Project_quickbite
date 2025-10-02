package com.quickbite.service;

import com.quickbite.dto.OrderDTO;
import com.quickbite.dto.OrderRequest;
import com.quickbite.exception.OrderNotFoundException;
import com.quickbite.model.MenuItem;
import com.quickbite.model.Order;
import com.quickbite.model.User;
import com.quickbite.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private MenuService menuService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private User testUser;
    private MenuItem testMenuItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");

        testMenuItem = new MenuItem();
        testMenuItem.setId("1");
        testMenuItem.setName("Test Pizza");
        testMenuItem.setPrice(199.0);

        testOrder = new Order();
        testOrder.setId("1");
        testOrder.setUser(testUser);
        testOrder.setMenuItem(testMenuItem);
        testOrder.setItemName("Test Pizza");
        testOrder.setQuantity(2);
        testOrder.setStatus("PENDING");
    }

    @Test
    void placeOrder_ShouldCreateAndReturnOrder() {
        // Given
        OrderRequest request = new OrderRequest();
        request.setUserId("1");
        request.setMenuItemId("1");
        request.setQuantity(2);

        when(userService.findById("1")).thenReturn(testUser);
        when(menuService.findById("1")).thenReturn(testMenuItem);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        OrderDTO result = orderService.placeOrder(request);

        // Then
        assertNotNull(result);
        assertEquals("Test Pizza", result.getItemName());
        assertEquals("PENDING", result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getOrdersByUserId_ShouldReturnUserOrders() {
        // Given
        List<Order> orders = List.of(testOrder);
        when(userService.findById("1")).thenReturn(testUser);
        when(orderRepository.findByUserId("1")).thenReturn(orders);

        // When
        List<OrderDTO> result = orderService.getOrdersByUserId("1");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Pizza", result.get(0).getItemName());
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Given
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<OrderDTO> result = orderService.getAllOrders();

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Pizza", result.get(0).getItemName());
    }

    @Test
    void getPendingOrders_ShouldReturnPendingOrders() {
        // Given
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findByStatus("PENDING")).thenReturn(orders);

        // When
        List<OrderDTO> result = orderService.getPendingOrders();

        // Then
        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    @Test
    void updateOrderStatus_ShouldUpdateAndReturnOrder_WhenOrderExists() {
        // Given
        when(orderRepository.findById("1")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(testOrder)).thenReturn(testOrder);

        // When
        OrderDTO result = orderService.updateOrderStatus("1", "APPROVED");

        // Then
        assertEquals("APPROVED", testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenOrderNotFound() {
        // Given
        when(orderRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class,
                () -> orderService.updateOrderStatus("999", "APPROVED"));
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenInvalidStatus() {
        // Given
        when(orderRepository.findById("1")).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrderStatus("1", "INVALID_STATUS"));
    }

    @Test
    void findById_ShouldReturnOrder_WhenOrderExists() {
        // Given
        when(orderRepository.findById("1")).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.findById("1");

        // Then
        assertEquals(testOrder, result);
    }

    @Test
    void findById_ShouldThrowException_WhenOrderNotFound() {
        // Given
        when(orderRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class,
                () -> orderService.findById("999"));
    }
}