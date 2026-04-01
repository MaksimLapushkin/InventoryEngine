package com.inventory.engine.repository;

import com.inventory.engine.model.StockItem;
import com.inventory.engine.model.StockKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<StockItem, StockKey> {
}