package com.maxlapushkin.inventory.repository;

import com.maxlapushkin.inventory.model.Product;
import com.maxlapushkin.inventory.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findByUnit(Unit unit);
    List<Product> findByNameContainingIgnoreCase(String name);
}