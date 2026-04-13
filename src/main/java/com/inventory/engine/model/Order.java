package com.inventory.engine.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> items = new ArrayList<>();

    protected Order() {
    }

    public Order(List<OrderLine> items) {
        this.status = OrderStatus.CREATED;
        this.warehouseId = null;
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
        for (OrderLine item : items) {
            addItem(item);
        }
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public List<OrderLine> getItems() {
        return items;
    }

    public void addItem(OrderLine item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        item.setOrder(this);
        items.add(item);
    }

    public void reserve(Long warehouseId) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("order must be in CREATED status to reserve");
        }
        if (warehouseId == null) {
            throw new IllegalArgumentException("warehouseId cannot be null");
        }
        this.status = OrderStatus.RESERVED;
        this.warehouseId = warehouseId;
    }

    public void releaseReservation() {
        if (status != OrderStatus.RESERVED) {
            throw new IllegalStateException("order must be in RESERVED status to release reservation");
        }
        if (warehouseId == null) {
            throw new IllegalStateException("reserved order must have a reservation warehouse");
        }
        this.status = OrderStatus.CREATED;
        this.warehouseId = null;
    }

    public void fulfill() {
        if (status != OrderStatus.RESERVED) {
            throw new IllegalStateException("order must be in RESERVED status to fulfill reservation");
        }
        if (warehouseId == null) {
            throw new IllegalStateException("reserved order must have a reservation warehouse");
        }
        this.status = OrderStatus.FULFILLED;
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("order is already cancelled");
        }
        if (status != OrderStatus.CREATED && status != OrderStatus.RESERVED) {
            throw new IllegalStateException("order cannot be cancelled from status " + status);
        }
        this.status = OrderStatus.CANCELLED;
        this.warehouseId = null;
    }
}
