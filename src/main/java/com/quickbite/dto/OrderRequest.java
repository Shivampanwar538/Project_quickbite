package com.quickbite.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long userId;
    private Long menuItemId;
    private int quantity=1;
}
