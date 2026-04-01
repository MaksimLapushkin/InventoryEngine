package com.inventory.engine.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockItem {

    @EmbeddedId
    private StockKey id;

    @Column(nullable = false)
    private int available;

    @Column(nullable = false)
    private int reserved;

    public StockItem(Long productId, Long warehouseId, int available, int reserved) {
        if (productId == null) {
            throw new IllegalArgumentException("productId cannot be null");
        }
        if (warehouseId == null) {
            throw new IllegalArgumentException("warehouseId cannot be null");
        }
        if (available < 0) {
            throw new IllegalArgumentException("available cannot be negative");
        }
        if (reserved < 0) {
            throw new IllegalArgumentException("reserved cannot be negative");
        }

        this.id = new StockKey(productId, warehouseId);
        this.available = available;
        this.reserved = reserved;
    }

    public Long getProductId() {
        return id.getProductId();
    }

    public Long getWarehouseId() {
        return id.getWarehouseId();
    }

    public int getTotal() {
        return reserved + available;
    }

    public void addStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be positive");
        }
        available += qty;
    }

    public void reserve(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be positive");
        }
        if (qty > available) {
            throw new IllegalStateException("not enough items available");
        }
        available -= qty;
        reserved += qty;
    }

    public void releaseReservation(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be positive");
        }
        if (qty > reserved) {
            throw new IllegalStateException("cannot release more items than reserved");
        }
        available += qty;
        reserved -= qty;
    }
}