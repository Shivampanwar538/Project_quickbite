package com.quickbite.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.config.SecurityConfig;
import com.quickbite.dto.MenuItemDTO;
import com.quickbite.model.MenuItem;
import com.quickbite.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser; // <-- ADDED IMPORT

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(MenuController.class)
@Import(SecurityConfig.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @Autowired
    private ObjectMapper objectMapper;

    private MenuItem testMenuItem;
    private MenuItemDTO testMenuItemDTO;

    @BeforeEach
    void setUp() {
        testMenuItem = new MenuItem();
        testMenuItem.setId("1");
        testMenuItem.setName("Margherita Pizza");
        testMenuItem.setDescription("Classic cheese and tomato");
        testMenuItem.setPrice(199.0);

        testMenuItemDTO = new MenuItemDTO("1", "Margherita Pizza",
                "Classic cheese and tomato", 199.0);
    }

    // --- GET /menu Tests (Public Access) ---

    @Test
    void getMenu_ShouldReturnAllMenuItems() throws Exception {
        // Given
        MenuItemDTO item1 = new MenuItemDTO("1", "Pizza", "Delicious", 199.0);
        MenuItemDTO item2 = new MenuItemDTO("2", "Burger", "Tasty", 149.0);
        List<MenuItemDTO> menuItems = Arrays.asList(item1, item2);

        when(menuService.getAllMenuItems()).thenReturn(menuItems);

        // When & Then
        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(menuService, times(1)).getAllMenuItems();
    }

    @Test
    void getMenu_ShouldReturnEmptyList_WhenNoItemsExist() throws Exception {
        // Given
        when(menuService.getAllMenuItems()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(menuService, times(1)).getAllMenuItems();
    }

    @Test
    void getMenu_ShouldHandleServiceException() throws Exception {
        // Given
        when(menuService.getAllMenuItems()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/menu"))
                .andExpect(status().isInternalServerError());

        verify(menuService, times(1)).getAllMenuItems();
    }

    // --- POST /menu Tests (Requires ADMIN Role + CSRF) ---

    @Test
    @WithMockUser(roles = "ADMIN") // <-- ADDED ADMIN MOCK USER
    void addItem_ShouldCreateNewMenuItem() throws Exception {
        // Given
        MenuItem newItem = new MenuItem();
        newItem.setName("Veggie Burger");
        newItem.setDescription("Healthy and tasty");
        newItem.setPrice(149.0);

        MenuItemDTO createdItemDTO = new MenuItemDTO("2", "Veggie Burger",
                "Healthy and tasty", 149.0);

        when(menuService.addMenuItem(any(MenuItem.class))).thenReturn(createdItemDTO);

        // When & Then
        mockMvc.perform(post("/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItem))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Veggie Burger")));

        verify(menuService, times(1)).addMenuItem(any(MenuItem.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // <-- ADDED ADMIN MOCK USER
    void addItem_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Given - Invalid item with short name and negative price
        MenuItem invalidItem = new MenuItem();
        invalidItem.setName("A");
        invalidItem.setDescription("Test");
        invalidItem.setPrice(-10.0);

        // When & Then
        mockMvc.perform(post("/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(menuService, never()).addMenuItem(any(MenuItem.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // <-- ADDED ADMIN MOCK USER
    void addItem_ShouldReturnBadRequest_WhenPriceIsZero() throws Exception {
        // Given
        MenuItem invalidItem = new MenuItem();
        invalidItem.setName("Test Item");
        invalidItem.setDescription("Test");
        invalidItem.setPrice(0.0);

        // When & Then
        mockMvc.perform(post("/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(menuService, never()).addMenuItem(any(MenuItem.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // <-- ADDED ADMIN MOCK USER
    void addItem_WithValidMinimumValues_ShouldSucceed() throws Exception {
        // Given - Minimum valid values
        MenuItem minItem = new MenuItem();
        minItem.setName("AB");
        minItem.setDescription("");
        minItem.setPrice(0.01);

        MenuItemDTO createdDTO = new MenuItemDTO("3", "AB", "", 0.01);

        when(menuService.addMenuItem(any(MenuItem.class))).thenReturn(createdDTO);

        // When & Then
        mockMvc.perform(post("/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minItem))
                        .with(csrf()))
                .andExpect(status().isCreated());

        verify(menuService, times(1)).addMenuItem(any(MenuItem.class));
    }

    // --- PUT /menu/{id} Tests (Requires ADMIN Role + CSRF) ---

    @Test
    @WithMockUser(roles = "ADMIN") // <-- ADDED ADMIN MOCK USER
    void updateItem_ShouldUpdateExistingMenuItem() throws Exception {
        // Given
        MenuItem updatedItem = new MenuItem();
        updatedItem.setName("Updated Pizza");
        updatedItem.setDescription("New description");
        updatedItem.setPrice(249.0);

        MenuItemDTO updatedDTO = new MenuItemDTO("1", "Updated Pizza",
                "New description", 249.0);

        when(menuService.updateMenuItem(eq("1"), any(MenuItem.class))).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(put("/menu/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Pizza")));

        verify(menuService, times(1)).updateMenuItem(eq("1"), any(MenuItem.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // <-- ADDED ADMIN MOCK USER
    void updateItem_ShouldReturnNotFound_WhenItemDoesNotExist() throws Exception {
        // Given
        MenuItem updatedItem = new MenuItem();
        updatedItem.setName("Updated Item");
        updatedItem.setPrice(199.0);

        when(menuService.updateMenuItem(eq("999"), any(MenuItem.class)))
                .thenThrow(new com.quickbite.exception.MenuItemNotFoundException("999"));

        // When & Then
        mockMvc.perform(put("/menu/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(menuService, times(1)).updateMenuItem(eq("999"), any(MenuItem.class));
    }

    // --- DELETE /menu/{id} Tests (Requires ADMIN Role + CSRF) ---

    @Test
    @WithMockUser(roles = "ADMIN") // <-- ADDED ADMIN MOCK USER
    void deleteItem_ShouldDeleteMenuItem() throws Exception {
        // Given
        doNothing().when(menuService).deleteMenuItem("1");

        // When & Then
        mockMvc.perform(delete("/menu/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(menuService, times(1)).deleteMenuItem("1");
    }

    @Test
    @WithMockUser(roles = "ADMIN") // <-- ADDED ADMIN MOCK USER
    void deleteItem_ShouldReturnNotFound_WhenItemDoesNotExist() throws Exception {
        // Given
        doThrow(new com.quickbite.exception.MenuItemNotFoundException("999"))
                .when(menuService).deleteMenuItem("999");

        // When & Then
        mockMvc.perform(delete("/menu/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(menuService, times(1)).deleteMenuItem("999");
    }

    // --- Security Forbidden/Unauthorized Tests (Highly recommended) ---

    @Test
    @WithMockUser(roles = "STUDENT") // Authenticated user lacking ADMIN role
    void addItem_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        // Given
        MenuItem newItem = new MenuItem();
        newItem.setName("Unauthorized Item");
        newItem.setPrice(10.0);

        // When & Then
        mockMvc.perform(post("/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItem))
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Expect 403 Forbidden

        verify(menuService, never()).addMenuItem(any());
    }

    @Test
    void deleteItem_ShouldReturnUnauthorized_WhenUnauthenticated() throws Exception {
        // When & Then
        // No @WithMockUser means unauthenticated
        mockMvc.perform(delete("/menu/1")
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Expect 403 Forbidden due to lack of authentication

        verify(menuService, never()).deleteMenuItem(any());
    }

}