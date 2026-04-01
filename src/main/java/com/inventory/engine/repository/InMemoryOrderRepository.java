package com.inventory.engine.repository;

import com.inventory.engine.model.Order;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> storage = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            order = new Order(idCounter++, order.getItems());
        }
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(storage.values());
    }
}