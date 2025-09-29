package com.quickbite.controller;

import org.springframework.web.bind.annotation.*;
import com.quickbite.repository.MenuRepository;
import com.quickbite.model.MenuItem;
import java.util.List;
import com.quickbite.dto.MenuItemDTO;

@RestController
@RequestMapping("/menu")
@CrossOrigin(origins = "*")
public class MenuController {
    private final MenuRepository menuRepo;

    public MenuController(MenuRepository menuRepo) {
        this.menuRepo = menuRepo;
    }

    @GetMapping
    public List<MenuItemDTO> getMenu() {
        return menuRepo.findAll().stream()
                .map(item -> new MenuItemDTO(item.getId(), item.getName(),
                        item.getDescription(), item.getPrice()))
                .toList();
    }

    @PostMapping
    public MenuItemDTO addItem(@RequestBody MenuItem item) {
        return new MenuItemDTO(menuRepo.save(item));
    }

    @PutMapping("/{id}")
    public MenuItemDTO updateItem(@PathVariable String id, @RequestBody MenuItem newItem) {
        MenuItem existing = menuRepo.findById(id).orElseThrow();
        existing.setName(newItem.getName());
        existing.setDescription(newItem.getDescription());
        existing.setPrice(newItem.getPrice());
        return new MenuItemDTO(menuRepo.save(existing));
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable String id) {
        menuRepo.deleteById(id);
    }
}