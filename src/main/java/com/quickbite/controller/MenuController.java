package com.quickbite.controller;

import com.quickbite.dto.MenuItemDTO;
import com.quickbite.model.MenuItem;
import com.quickbite.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuItemDTO>> getMenu() {
        List<MenuItemDTO> menuItems = menuService.getAllMenuItems();
        return ResponseEntity.ok(menuItems);
    }

    @PostMapping
    public ResponseEntity<MenuItemDTO> addItem(@Valid @RequestBody MenuItem item) {
        MenuItemDTO addedItem = menuService.addMenuItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemDTO> updateItem(@PathVariable String id,
                                                  @Valid @RequestBody MenuItem newItem) {
        MenuItemDTO updatedItem = menuService.updateMenuItem(id, newItem);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}