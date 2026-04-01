package com.inventory.engine.exception;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(Long productId, Long warehouseId) {
        super("Stock not found for productId=" + productId + ", warehouseId=" + warehouseId);
    }
}