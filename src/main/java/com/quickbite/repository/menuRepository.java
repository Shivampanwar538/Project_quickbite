package com.quickbite.repository;
import com.quickbite.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface menuRepository extends JpaRepository<MenuItem, Long> {}
