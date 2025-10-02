package com.quickbite.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Menu item ID is required")
    private String menuItemId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;
}