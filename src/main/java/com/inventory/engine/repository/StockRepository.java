package com.inventory.engine.repository;

import com.inventory.engine.model.StockItem;
import com.inventory.engine.model.StockKey;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    void save(StockItem item);
    Optional<StockItem> findByKey(StockKey key);
    List<StockItem> findAll();
}
