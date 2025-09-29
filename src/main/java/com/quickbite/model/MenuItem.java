package com.quickbite.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

@Data
@Document(collection = "menu_items")
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    @Id
    private String id;  // MongoDB uses String ID

    private String name;
    private String description;
    private double price;

    @DBRef
    private List<Order> orders;
}