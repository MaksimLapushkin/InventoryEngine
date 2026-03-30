package com.inventory.engine.repository;

import com.inventory.engine.model.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryProductRepository implements ProductRepository {
    private final Map<Integer, Product> storage = new HashMap<>();

    @Override
    public void save(Product product) {
    storage.put(product.getId(), product);
    }

    @Override
    public Optional<Product> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }
}
