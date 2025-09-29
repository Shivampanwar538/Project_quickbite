package com.quickbite.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private String userId;  // Changed from Long to String
    private String menuItemId;  // Changed from Long to String
    private int quantity = 1;
}