package com.quickbite.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "menu_item")
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private double price;
    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL)
    private java.util.List<Order> orders;

}
