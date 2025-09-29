package com.quickbite.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;  // MongoDB uses String ID by default

    @Indexed(unique = true)  // Ensures username uniqueness
    private String username;

    private String password;
    private String role = "STUDENT";

    @DBRef
    @JsonIgnore  // Prevent infinite recursion
    private List<Order> orders = new ArrayList<>();
}