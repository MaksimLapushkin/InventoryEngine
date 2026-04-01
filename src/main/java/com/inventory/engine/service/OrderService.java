package com.inventory.engine.service;

import com.inventory.engine.dto.CreateOrderRequest;
import com.inventory.engine.dto.OrderLineResponse;
import com.inventory.engine.dto.OrderResponse;
import com.inventory.engine.exception.OrderNotFoundException;
import com.inventory.engine.model.Order;
import com.inventory.engine.model.OrderLine;
import com.inventory.engine.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository repository;
    private final StockService stockService;

    public OrderService(OrderRepository repository, StockService stockService) {
        this.repository = repository;
        this.stockService = stockService;
    }

    public OrderResponse createOrder(CreateOrderRequest request){
        List<OrderLine> lines = request.getLines().stream()
                .map(l -> new OrderLine(l.getProductId(), l.getQuantity()))
                .toList();

        Order order = new Order(null,lines);
        return toResponse(repository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getItems().stream()
                        .map(i-> new OrderLineResponse(i.getProductId(), i.getQuantity()))
                        .toList(),
                order.getStatus().name()
        );
    }

    public OrderResponse findById(Long orderId){
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return toResponse(order);
    }

    public OrderResponse reserve(Long orderId, Long warehouseId){
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        stockService.reserveOrderAtomically(warehouseId,order);
        repository.save(order);

        return toResponse(order);
    }
}
