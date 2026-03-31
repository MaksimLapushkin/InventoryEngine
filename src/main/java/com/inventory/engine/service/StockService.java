package com.inventory.engine.service;
import com.inventory.engine.dto.StockResponse;
import com.inventory.engine.model.*;
import com.inventory.engine.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    private final StockRepository repository;

    public StockService(StockRepository repository) {
        this.repository = repository;
    }

    public void addStock(Long productId, Long warehouseId, int qty) {
        StockKey key = new StockKey(productId,warehouseId);

        StockItem stockItem = repository
                .findByKey(key)
                .orElseGet(() -> new StockItem(productId, warehouseId, 0, 0));
        stockItem.addStock(qty);
        repository.save(stockItem);
    }

    public void reserveStock(Long productId, Long warehouseId, int qty) {
        StockKey key = new StockKey(productId,warehouseId);

        StockItem stockItem = repository
                .findByKey(key)
                .orElseThrow(() -> new IllegalStateException("stock not found"));
        stockItem.reserve(qty);
        repository.save(stockItem);
    }

    public void releaseStock(Long productId, Long warehouseId, int qty) {
        StockKey key = new StockKey(productId,warehouseId);
        StockItem stockItem = repository
                .findByKey(key)
                .orElseThrow(() -> new IllegalStateException("stock not found"));
        stockItem.releaseReservation(qty);
        repository.save(stockItem);
    }

    public void reserveOrder(Long warehouseId, Order order){
        List<OrderLine> items = order.getItems();
        for (OrderLine line : items){
            reserveStock(line.getProductId(), warehouseId, line.getQuantity());
        }
        order.reserve();
    }
    public void releaseOrderReservation(Long warehouseId, Order order){
        List<OrderLine> items = order.getItems();
        for (OrderLine line : items){
            releaseStock(line.getProductId(), warehouseId, line.getQuantity());
        }
        order.cancel();
    }

    public void reserveOrderAtomically(Long warehouseId, Order order) {

        List<OrderLine> items = order.getItems();

        // phase 1 validation

        for (OrderLine line : items) {

            StockKey key = new StockKey(line.getProductId(), warehouseId);

            StockItem stockItem = repository
                    .findByKey(key)
                    .orElseThrow(() -> new IllegalStateException("Stock not found"));

            if (stockItem.getAvailable() < line.getQuantity()) {
                throw new IllegalStateException("Not enough stock");
            }
        }

        // phase 2 commit

        for (OrderLine line : items) {
            reserveStock(line.getProductId(), warehouseId, line.getQuantity());
        }

        order.reserve();
    }

    public List<StockResponse> getStocks(Long productId, Long warehouseId) {
        return repository.findAll().stream()
                .filter(stockItem -> productId == null || stockItem.getProductId().equals(productId))
                .filter(stockItem -> warehouseId == null || stockItem.getWarehouseId().equals(warehouseId))
                .map(stockItem -> new StockResponse(
                        stockItem.getProductId(),
                        stockItem.getWarehouseId(),
                        stockItem.getTotal(),
                        stockItem.getReserved(),
                        stockItem.getAvailable()
                ))
                .toList();
    }

}