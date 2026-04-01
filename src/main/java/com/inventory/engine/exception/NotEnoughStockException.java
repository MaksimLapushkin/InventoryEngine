package com.inventory.engine.exception;

public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException(Long productId, Long warehouseId) {
        super("Not enough stock for productId=" + productId + ", warehouseId=" + warehouseId);
    }
}