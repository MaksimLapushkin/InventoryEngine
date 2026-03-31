package com.inventory.engine.repository;
import com.inventory.engine.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    void save(Product product);
    Optional<Product>findById(Long id);
    List<Product>findAll();
}
