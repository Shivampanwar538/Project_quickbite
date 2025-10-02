package com.quickbite.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
@Document(collection = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private String id;  // MongoDB uses String ID

    private String itemName;
    private int quantity;
    private String status;

    @DBRef
    private User user;

    @DBRef
    private MenuItem menuItem;
}