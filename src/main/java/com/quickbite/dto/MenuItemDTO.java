package com.quickbite.dto;

import com.quickbite.model.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemDTO {
    private String id;  // Changed from Long to String
    private String name;
    private String description;
    private double price;

    public MenuItemDTO(MenuItem item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.price = item.getPrice();
    }
}
