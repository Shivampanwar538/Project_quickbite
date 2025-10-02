package com.quickbite.service;

import com.quickbite.dto.MenuItemDTO;
import com.quickbite.exception.MenuItemNotFoundException;
import com.quickbite.model.MenuItem;
import com.quickbite.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;

    public List<MenuItemDTO> getAllMenuItems() {
        log.info("Fetching all menu items");
        return menuRepository.findAll().stream()
                .map(item -> new MenuItemDTO(item.getId(), item.getName(),
                        item.getDescription(), item.getPrice()))
                .toList();
    }

    public MenuItemDTO addMenuItem(MenuItem menuItem) {
        log.info("Adding new menu item: {}", menuItem.getName());
        MenuItem savedItem = menuRepository.save(menuItem);
        log.info("Menu item added successfully with id: {}", savedItem.getId());
        return new MenuItemDTO(savedItem);
    }

    public MenuItemDTO updateMenuItem(String id, MenuItem updatedItem) {
        log.info("Updating menu item with id: {}", id);

        MenuItem existingItem = menuRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));

        existingItem.setName(updatedItem.getName());
        existingItem.setDescription(updatedItem.getDescription());
        existingItem.setPrice(updatedItem.getPrice());

        MenuItem savedItem = menuRepository.save(existingItem);
        log.info("Menu item updated successfully: {}", savedItem.getName());

        return new MenuItemDTO(savedItem);
    }

    public void deleteMenuItem(String id) {
        log.info("Deleting menu item with id: {}", id);

        if (!menuRepository.existsById(id)) {
            throw new MenuItemNotFoundException(id);
        }

        menuRepository.deleteById(id);
        log.info("Menu item deleted successfully with id: {}", id);
    }

    public MenuItem findById(String id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
    }
}