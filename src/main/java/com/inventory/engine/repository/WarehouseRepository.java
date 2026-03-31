package com.inventory.engine.repository;

import com.inventory.engine.model.Warehouse;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository {
    Warehouse save(Warehouse warehouse);

    List<Warehouse> findAll();

    Optional<Warehouse> findById(Long id);
}
