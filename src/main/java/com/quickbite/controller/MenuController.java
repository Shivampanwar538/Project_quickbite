package com.quickbite.controller;

import org.springframework.web.bind.annotation.*;
import com.quickbite.repository.menuRepository;
import com.quickbite.model.MenuItem;
import java.util.List;
import com.quickbite.dto.MenuItemDTO;

@RestController
@RequestMapping("/menu")
public class MenuController {
    private final menuRepository menuRepo;
    public MenuController(menuRepository menuRepo) { this.menuRepo = menuRepo; }

    // Get full menu
    @GetMapping
    public List<MenuItemDTO> getMenu() {
        return menuRepo.findAll().stream()
                .map(item -> new MenuItemDTO(item.getId(), item.getName(), item.getDescription(), item.getPrice()))
                .toList();
    }


    // Add new item
    @PostMapping
    public MenuItemDTO addItem(@RequestBody MenuItem item) {
        return new MenuItemDTO(menuRepo.save(item));
    }
    // Update item
    @PutMapping("/{id}")
    public MenuItemDTO updateItem(@PathVariable Long id, @RequestBody MenuItem newItem) {
        MenuItem existing = menuRepo.findById(id).orElseThrow();
        existing.setName(newItem.getName());
        existing.setDescription(newItem.getDescription());
        existing.setPrice(newItem.getPrice());
        return new MenuItemDTO(menuRepo.save(existing));
    }

    // Delete item
    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        menuRepo.deleteById(id);
    }
}
