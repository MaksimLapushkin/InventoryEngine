package com.inventory.engine.service;

import com.inventory.engine.exception.WarehouseNotFoundException;
import com.inventory.engine.model.Warehouse;
import com.inventory.engine.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository repository;

    @Transactional
    public Warehouse create(String name) {
        return repository.save(new Warehouse(name));
    }

    public List<Warehouse> getAll() {
        return repository.findAll();
    }

    public Warehouse getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
    }
}
