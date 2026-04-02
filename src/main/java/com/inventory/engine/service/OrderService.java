package com.inventory.engine.service;

import com.inventory.engine.dto.CreateOrderRequest;
import com.inventory.engine.dto.OrderResponse;
import com.inventory.engine.exception.OrderNotFoundException;
import com.inventory.engine.mapper.OrderMapper;
import com.inventory.engine.model.Order;
import com.inventory.engine.model.OrderLine;
import com.inventory.engine.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final StockService stockService;

    public OrderService(OrderRepository repository, StockService stockService) {
        this.repository = repository;
        this.stockService = stockService;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        List<OrderLine> lines = request.getLines().stream()
                .map(l -> new OrderLine(l.getProductId(), l.getQuantity()))
                .toList();

        Order order = new Order(lines);

        return OrderMapper.toResponse(repository.save(order));
    }

    public OrderResponse findById(Long orderId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return OrderMapper.toResponse(order);
    }

    public List<OrderResponse> listOrders() {
        return OrderMapper.toResponseList(repository.findAll());
    }

    @Transactional
    public OrderResponse reserve(Long orderId, Long warehouseId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        stockService.reserveOrderAtomically(warehouseId, order);
        repository.save(order);

        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long orderId, Long warehouseId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        stockService.releaseOrderReservation(warehouseId, order);
        repository.save(order);

        return OrderMapper.toResponse(order);
    }
}
