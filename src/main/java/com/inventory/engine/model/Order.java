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

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> items = new ArrayList<>();

    protected Order() {
    }

    public Order(List<OrderLine> items) {
        this.status = OrderStatus.NEW;
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

    public void reserve() {
        if (getStatus() != OrderStatus.NEW) {
            throw new IllegalArgumentException("wrong status");
        }
        this.status = OrderStatus.RESERVED;
    }

    public void confirm() {
        if (getStatus() != OrderStatus.RESERVED) {
            throw new IllegalArgumentException("wrong status");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("order is already cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }
}