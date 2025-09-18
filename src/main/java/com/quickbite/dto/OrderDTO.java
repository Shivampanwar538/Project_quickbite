package com.quickbite.dto;

import com.quickbite.model.Order;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private String itemName;
    private String status;
    private String username;


    public OrderDTO(Long id, String itemName, String status, String username) {
        this.id = id;
        this.itemName = itemName;
        this.status = status;
        this.username = username;
    }

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.itemName = order.getMenuItem().getName();
        this.status = order.getStatus();
        this.username = order.getUser().getUsername();
    }
}
