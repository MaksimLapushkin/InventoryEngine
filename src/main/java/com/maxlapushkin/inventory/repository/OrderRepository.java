package com.maxlapushkin.inventory.repository;

import com.maxlapushkin.inventory.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}