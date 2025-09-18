package com.quickbite.repository;

import com.quickbite.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface orderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    Collection<Object> findByStatus(String pending);
}
