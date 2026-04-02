package com.inventory.engine.service;
import com.inventory.engine.dto.StockResponse;
import com.inventory.engine.exception.NotEnoughStockException;
import com.inventory.engine.exception.StockNotFoundException;
import com.inventory.engine.model.*;
import com.inventory.engine.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class StockService {

    private final StockRepository repository;

    public StockService(StockRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void addStock(Long productId, Long warehouseId, int qty) {
        validateQty(qty);

        int updated = repository.increaseAvailable(productId, warehouseId, qty);
        if (updated == 0) {
            StockItem stockItem = new StockItem(productId, warehouseId, 0, 0);
            stockItem.addStock(qty);
            try {
                repository.save(stockItem);
            } catch (DataIntegrityViolationException ex) {
                repository.increaseAvailable(productId, warehouseId, qty);
            }
        }
    }

    @Transactional
    public void reserveStock(Long productId, Long warehouseId, int qty) {
        validateQty(qty);
        StockKey key = new StockKey(productId,warehouseId);

        int updated = repository.reserveStock(productId, warehouseId, qty);
        if (updated == 0) {
            if (repository.existsById(key)) {
                throw new NotEnoughStockException(productId, warehouseId);
            }
            throw new StockNotFoundException(productId, warehouseId);
        }
    }

    @Transactional
    public void releaseStock(Long productId, Long warehouseId, int qty) {
        validateQty(qty);
        StockKey key = new StockKey(productId,warehouseId);
        int updated = repository.releaseStock(productId, warehouseId, qty);
        if (updated == 0) {
            if (repository.existsById(key)) {
                throw new IllegalStateException("cannot release more items than reserved");
            }
            throw new StockNotFoundException(productId, warehouseId);
        }
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
        List<OrderLine> items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderLine::getProductId))
                .toList();

        for (OrderLine line : items) {
            reserveStock(line.getProductId(), warehouseId, line.getQuantity());
        }

        order.reserve();
    }

    public List<StockResponse> getStocks(Long productId, Long warehouseId) {
        List<StockItem> items;
        if (productId != null && warehouseId != null) {
            items = repository.findByIdProductIdAndIdWarehouseId(productId, warehouseId);
        } else if (productId != null) {
            items = repository.findByIdProductId(productId);
        } else if (warehouseId != null) {
            items = repository.findByIdWarehouseId(warehouseId);
        } else {
            items = repository.findAll();
        }

        return items.stream()
                .map(stockItem -> new StockResponse(
                        stockItem.getProductId(),
                        stockItem.getWarehouseId(),
                        stockItem.getTotal(),
                        stockItem.getReserved(),
                        stockItem.getAvailable()
                ))
                .toList();
    }

    private void validateQty(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be positive");
        }
    }
}
