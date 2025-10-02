package com.quickbite.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.config.SecurityConfig; // <-- ADDED
import com.quickbite.dto.OrderDTO;
import com.quickbite.dto.OrderRequest;
import com.quickbite.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import; // <-- ADDED
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // <-- ADDED
import org.springframework.test.web.servlet.MockMvc;

// Assuming these exceptions exist in the quickbite.exception package
import com.quickbite.exception.UserNotFoundException;
import com.quickbite.exception.MenuItemNotFoundException;
import com.quickbite.exception.OrderNotFoundException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // <-- ADDED

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class) // <-- ADDED
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDTO testOrderDTO;
    private OrderRequest testOrderRequest;

    @BeforeEach
    void setUp() {
        testOrderDTO = new OrderDTO();
        testOrderDTO.setId("1");
        testOrderDTO.setItemName("Margherita Pizza");
        testOrderDTO.setQuantity(2);
        testOrderDTO.setStatus("PENDING");
        testOrderDTO.setUsername("testuser");

        testOrderRequest = new OrderRequest();
        testOrderRequest.setUserId("user1");
        testOrderRequest.setMenuItemId("item1");
        testOrderRequest.setQuantity(2);
    }

    // --- POST /order/place Tests (Requires CSRF) ---
    @Test
    // Assuming unauthenticated users can place orders, but CSRF is required
    @WithMockUser(roles = "STUDENT")
    void placeOrder_ShouldCreateNewOrder() throws Exception {
        // Given
        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(testOrderDTO);

        // When & Then
        mockMvc.perform(post("/order/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest))
                        .with(csrf())) // <-- ADDED CSRF
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.itemName", is("Margherita Pizza")))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.username", is("testuser")));

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void placeOrder_ShouldReturnBadRequest_WhenUserIdMissing() throws Exception {
        // Given - Request without userId (assuming @NotBlank or @NotNull validation)
        OrderRequest invalidRequest = new OrderRequest();
        invalidRequest.setMenuItemId("item1");
        invalidRequest.setQuantity(1);
        invalidRequest.setUserId(null);

        // When & Then
        mockMvc.perform(post("/order/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf())) // <-- ADDED CSRF
                .andExpect(status().isBadRequest());

        verify(orderService, never()).placeOrder(any(OrderRequest.class));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void placeOrder_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Given
        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenThrow(new UserNotFoundException("user999", "id"));

        // When & Then
        mockMvc.perform(post("/order/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest))
                        .with(csrf())) // <-- ADDED CSRF
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class));
    }

    // --- GET /order/user/{userId} Tests (Requires Authentication) ---
    @Test
    @WithMockUser(username = "user1", roles = "STUDENT") // Must be authenticated to view their orders
    void getOrdersByUser_ShouldReturnUserOrders() throws Exception {
        // Given (rest of the setup is the same)
        List<OrderDTO> orders = Arrays.asList(
                new OrderDTO("1", "Pizza", "PENDING", "user1",1),
                new OrderDTO("2", "Burger", "COMPLETED", "user1",1)
        );
        String userId = "user1";

        when(orderService.getOrdersByUserId(userId)).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/order/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].itemName", is("Pizza")));

        verify(orderService, times(1)).getOrdersByUserId(userId);
    }

    // --- GET /order/all Tests (Requires ADMIN Role) ---
    @Test
    @WithMockUser(roles = "ADMIN") // Assuming only ADMIN can view all orders
    void getAllOrders_ShouldReturnAllOrders() throws Exception {
        // Given
        List<OrderDTO> allOrders = Arrays.asList(
                new OrderDTO("1", "Pizza", "PENDING", "user1",1),
                new OrderDTO("2", "Burger", "COMPLETED", "user2",1)
        );

        when(orderService.getAllOrders()).thenReturn(allOrders);

        // When & Then
        mockMvc.perform(get("/order/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].itemName", is("Pizza")));

        verify(orderService, times(1)).getAllOrders();
    }

    // --- GET /order/pending Tests (Requires ADMIN Role) ---
    @Test
    @WithMockUser(roles = "ADMIN") // Assuming ADMIN can view pending orders
    void getPendingOrders_ShouldReturnOnlyPendingOrders() throws Exception {
        // Given
        List<OrderDTO> pendingOrders = Arrays.asList(
                new OrderDTO("1", "Pizza", "PENDING", "user1",1),
                new OrderDTO("2", "Burger", "PENDING", "user2",1)
        );

        when(orderService.getPendingOrders()).thenReturn(pendingOrders);

        // When & Then
        mockMvc.perform(get("/order/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));

        verify(orderService, times(1)).getPendingOrders();
    }

    // --- PUT /{id}/status Tests (Requires CSRF and ADMIN Role) ---
    @Test
    @WithMockUser(roles = "ADMIN") // Assuming ADMIN can update order status
    void updateStatus_ShouldUpdateOrderStatus() throws Exception {
        // Given
        String orderId = "1";
        String newStatus = "APPROVED";
        OrderDTO updatedOrder = new OrderDTO();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(newStatus);

        when(orderService.updateOrderStatus(orderId, newStatus)).thenReturn(updatedOrder);

        // When & Then
        mockMvc.perform(put("/order/{id}/status", orderId)
                        .param("status", newStatus)
                        .with(csrf())) // <-- ADDED CSRF
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(newStatus)));

        verify(orderService, times(1)).updateOrderStatus(orderId, newStatus);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_ShouldReturnNotFound_WhenOrderDoesNotExist() throws Exception {
        // Given
        String orderId = "999";
        String newStatus = "COMPLETED";
        when(orderService.updateOrderStatus(eq(orderId), eq(newStatus)))
                .thenThrow(new OrderNotFoundException(orderId));

        // When & Then
        mockMvc.perform(put("/order/{id}/status", orderId)
                        .param("status", newStatus)
                        .with(csrf())) // <-- ADDED CSRF
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).updateOrderStatus(orderId, newStatus);
    }

    // --- Security Failure Example ---
    @Test
    void getAllOrders_ShouldReturnForbidden_WhenUnauthenticated() throws Exception {
        // When & Then
        // No @WithMockUser means unauthenticated user
        mockMvc.perform(get("/order/all"))
                .andExpect(status().isForbidden()); // Assuming SecurityConfig redirects to unauthorized

        verify(orderService, never()).getAllOrders();
    }
}