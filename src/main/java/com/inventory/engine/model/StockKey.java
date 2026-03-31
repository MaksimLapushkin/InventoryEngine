package com.inventory.engine.model;

import java.util.Objects;

public class StockKey {

    private final Long productId;
    private final Long warehouseId;

    public StockKey(Long productId, Long warehouseId) {
        this.productId = productId;
        this.warehouseId = warehouseId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    @Override
    public boolean equals(Object  obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StockKey Sk = (StockKey) obj;
        return (Sk.productId==productId && Sk.warehouseId==warehouseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, warehouseId);
    }
}