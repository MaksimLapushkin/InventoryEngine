package com.inventory.engine.repository;

import com.inventory.engine.model.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryStockRepository implements StockRepository {
    private final Map<StockKey, StockItem> storage = new HashMap<>();
    @Override
    public void save(StockItem item) {
        StockKey key = new StockKey(item.getProductId(),item.getWarehouseId());
        storage.put(key,item);
    }

    @Override
    public Optional<StockItem> findByKey(StockKey key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public List<StockItem> findAll() {
        return new ArrayList<>(storage.values());
    }
}
