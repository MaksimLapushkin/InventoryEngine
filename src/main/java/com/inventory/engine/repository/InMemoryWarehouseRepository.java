package com.inventory.engine.repository;

import com.inventory.engine.model.Warehouse;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryWarehouseRepository implements WarehouseRepository {

    private final Map<Long, Warehouse> storage = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Warehouse save(Warehouse warehouse) {
        if (warehouse.getId() == null) {
            warehouse.setId(idCounter++);
        }
        storage.put(warehouse.getId(), warehouse);
        return warehouse;
    }

    @Override
    public List<Warehouse> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Warehouse> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
}

