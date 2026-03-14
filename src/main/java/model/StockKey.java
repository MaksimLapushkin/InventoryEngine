package model;

import java.util.Objects;

public class StockKey {

    private final int productId;
    private final int warehouseId;

    public StockKey(int productId, int warehouseId) {
        this.productId = productId;
        this.warehouseId = warehouseId;
    }

    public int getProductId() {
        return productId;
    }

    public int getWarehouseId() {
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