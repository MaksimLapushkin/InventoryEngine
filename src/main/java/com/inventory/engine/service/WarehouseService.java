package com.inventory.engine.service;

import com.inventory.engine.exception.WarehouseNotFoundException;
import com.inventory.engine.model.Warehouse;
import com.inventory.engine.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository repository;

    public Warehouse create(String name) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(name);
        return repository.save(warehouse);
    }

    public List<Warehouse> getAll() {
        return repository.findAll();
    }

    public Warehouse getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
    }
}