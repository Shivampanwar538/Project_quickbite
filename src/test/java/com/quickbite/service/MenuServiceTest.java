package com.quickbite.service;

import com.quickbite.dto.MenuItemDTO;
import com.quickbite.exception.MenuItemNotFoundException;
import com.quickbite.model.MenuItem;
import com.quickbite.repository.MenuRepository;
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
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuService menuService;

    private MenuItem testMenuItem;

    @BeforeEach
    void setUp() {
        testMenuItem = new MenuItem();
        testMenuItem.setId("1");
        testMenuItem.setName("Test Pizza");
        testMenuItem.setDescription("Delicious test pizza");
        testMenuItem.setPrice(199.0);
    }

    @Test
    void getAllMenuItems_ShouldReturnAllMenuItems() {
        // Given
        List<MenuItem> menuItems = List.of(testMenuItem);
        when(menuRepository.findAll()).thenReturn(menuItems);

        // When
        List<MenuItemDTO> result = menuService.getAllMenuItems();

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Pizza", result.get(0).getName());
        assertEquals(199.0, result.get(0).getPrice());
    }

    @Test
    void addMenuItem_ShouldAddAndReturnMenuItem() {
        // Given
        MenuItem newItem = new MenuItem();
        newItem.setName("New Pizza");
        newItem.setDescription("Brand new pizza");
        newItem.setPrice(249.0);

        when(menuRepository.save(any(MenuItem.class))).thenReturn(testMenuItem);

        // When
        MenuItemDTO result = menuService.addMenuItem(newItem);

        // Then
        assertNotNull(result);
        verify(menuRepository).save(newItem);
    }

    @Test
    void updateMenuItem_ShouldUpdateAndReturnMenuItem_WhenItemExists() {
        // Given
        MenuItem updatedItem = new MenuItem();
        updatedItem.setName("Updated Pizza");
        updatedItem.setDescription("Updated description");
        updatedItem.setPrice(299.0);

        when(menuRepository.findById("1")).thenReturn(Optional.of(testMenuItem));
        when(menuRepository.save(testMenuItem)).thenReturn(testMenuItem);

        // When
        MenuItemDTO result = menuService.updateMenuItem("1", updatedItem);

        // Then
        assertEquals("Updated Pizza", testMenuItem.getName());
        assertEquals("Updated description", testMenuItem.getDescription());
        assertEquals(299.0, testMenuItem.getPrice());
        verify(menuRepository).save(testMenuItem);
    }

    @Test
    void updateMenuItem_ShouldThrowException_WhenItemNotFound() {
        // Given
        MenuItem updatedItem = new MenuItem();
        when(menuRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MenuItemNotFoundException.class,
                () -> menuService.updateMenuItem("999", updatedItem));
    }

    @Test
    void deleteMenuItem_ShouldDeleteMenuItem_WhenItemExists() {
        // Given
        when(menuRepository.existsById("1")).thenReturn(true);

        // When
        menuService.deleteMenuItem("1");

        // Then
        verify(menuRepository).deleteById("1");
    }

    @Test
    void deleteMenuItem_ShouldThrowException_WhenItemNotFound() {
        // Given
        when(menuRepository.existsById("999")).thenReturn(false);

        // When & Then
        assertThrows(MenuItemNotFoundException.class,
                () -> menuService.deleteMenuItem("999"));
    }

    @Test
    void findById_ShouldReturnMenuItem_WhenItemExists() {
        // Given
        when(menuRepository.findById("1")).thenReturn(Optional.of(testMenuItem));

        // When
        MenuItem result = menuService.findById("1");

        // Then
        assertEquals(testMenuItem, result);
    }

    @Test
    void findById_ShouldThrowException_WhenItemNotFound() {
        // Given
        when(menuRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MenuItemNotFoundException.class,
                () -> menuService.findById("999"));
    }
}