package com.inventory.engine.exception;

public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException(Long id) {
        super("Warehouse not found: " + id);
    }
}
