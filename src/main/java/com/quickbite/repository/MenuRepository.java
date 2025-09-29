package com.quickbite.repository;

import com.quickbite.model.MenuItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends MongoRepository<MenuItem, String> {
    // You can add custom query methods here if needed
    // Example: List<MenuItem> findByPriceLessThan(double price);
}