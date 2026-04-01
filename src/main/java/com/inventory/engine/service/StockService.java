package com.inventory.engine.service;
import com.inventory.engine.dto.StockResponse;
import com.inventory.engine.exception.NotEnoughStockException;
import com.inventory.engine.exception.StockNotFoundException;
import com.inventory.engine.model.*;
import com.inventory.engine.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    private final StockRepository repository;

    public StockService(StockRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void addStock(Long productId, Long warehouseId, int qty) {
        StockKey key = new StockKey(productId,warehouseId);

        StockItem stockItem = repository
                .findById(key)
                .orElseGet(() -> new StockItem(productId, warehouseId, 0, 0));
        stockItem.addStock(qty);
        repository.save(stockItem);
    }

    @Transactional
    public void reserveStock(Long productId, Long warehouseId, int qty) {
        StockKey key = new StockKey(productId,warehouseId);

        StockItem stockItem = repository
                .findById(key)
                .orElseThrow(() -> new StockNotFoundException(productId, warehouseId));
        stockItem.reserve(qty);
        repository.save(stockItem);
    }

    @Transactional
    public void releaseStock(Long productId, Long warehouseId, int qty) {
        StockKey key = new StockKey(productId,warehouseId);
        StockItem stockItem = repository
                .findById(key)
                .orElseThrow(() -> new StockNotFoundException(productId, warehouseId));
        stockItem.releaseReservation(qty);
        repository.save(stockItem);
    }

    @Transactional
    public void reserveOrder(Long warehouseId, Order order){
        List<OrderLine> items = order.getItems();
        for (OrderLine line : items){
            reserveStock(line.getProductId(), warehouseId, line.getQuantity());
        }
        order.reserve();
    }

    @Transactional
    public void releaseOrderReservation(Long warehouseId, Order order){
        List<OrderLine> items = order.getItems();
        for (OrderLine line : items){
            releaseStock(line.getProductId(), warehouseId, line.getQuantity());
        }
        order.cancel();
    }

    @Transactional
    public void reserveOrderAtomically(Long warehouseId, Order order) {

        List<OrderLine> items = order.getItems();

        // validation

        for (OrderLine line : items) {

            StockKey key = new StockKey(line.getProductId(), warehouseId);

            StockItem stockItem = repository
                    .findById(key)
                    .orElseThrow(() -> new StockNotFoundException(line.getProductId(), warehouseId));

            if (stockItem.getAvailable() < line.getQuantity()) {
                throw new NotEnoughStockException(line.getProductId(), warehouseId);
            }
        }

        // commit

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