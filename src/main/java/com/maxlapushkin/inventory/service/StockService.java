package com.maxlapushkin.inventory.service;
import com.maxlapushkin.inventory.dto.StockResponse;
import com.maxlapushkin.inventory.exception.NotEnoughStockException;
import com.maxlapushkin.inventory.exception.StockNotFoundException;
import com.maxlapushkin.inventory.model.*;
import com.maxlapushkin.inventory.repository.StockRepository;
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
    public void fulfillStock(Long productId, Long warehouseId, int qty) {
        validateQty(qty);
        StockKey key = new StockKey(productId,warehouseId);
        int updated = repository.fulfillStock(productId, warehouseId, qty);
        if (updated == 0) {
            if (repository.existsById(key)) {
                throw new IllegalStateException("cannot fulfill more items than reserved");
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
    public void releaseOrderReservation(Long warehouseId, Order order){
        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new IllegalStateException("order must be in RESERVED status to release reservation");
        }

        List<OrderLine> items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderLine::getProductId))
                .toList();

        for (OrderLine line : items){
            releaseStock(line.getProductId(), warehouseId, line.getQuantity());
        }
        order.releaseReservation();
    }

    @Transactional
    public void fulfillOrder(Long warehouseId, Order order) {
        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new IllegalStateException("order must be in RESERVED status to fulfill reservation");
        }

        List<OrderLine> items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderLine::getProductId))
                .toList();

        for (OrderLine line : items){
            fulfillStock(line.getProductId(), warehouseId, line.getQuantity());
        }
        order.fulfill();
    }

    @Transactional
    public void reserveOrderAtomically(Long warehouseId, Order order) {
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException("order must be in CREATED status to reserve");
        }

        List<OrderLine> items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderLine::getProductId))
                .toList();

        for (OrderLine line : items) {
            reserveStock(line.getProductId(), warehouseId, line.getQuantity());
        }

        order.reserve(warehouseId);
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
