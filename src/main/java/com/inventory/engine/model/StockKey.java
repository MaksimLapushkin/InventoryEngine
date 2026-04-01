package com.inventory.engine.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StockKey implements Serializable {

    private Long productId;
    private Long warehouseId;

    protected StockKey() {}

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockKey that)) return false;
        return Objects.equals(productId, that.productId)
                && Objects.equals(warehouseId, that.warehouseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, warehouseId);
    }
}