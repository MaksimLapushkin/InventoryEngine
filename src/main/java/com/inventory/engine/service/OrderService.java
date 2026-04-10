package com.inventory.engine.service;

import com.inventory.engine.dto.CreateOrderRequest;
import com.inventory.engine.dto.OrderResponse;
import com.inventory.engine.exception.OrderNotFoundException;
import com.inventory.engine.mapper.OrderMapper;
import com.inventory.engine.messaging.OrderLifecycleEvent;
import com.inventory.engine.messaging.OrderLifecycleEventPublisher;
import com.inventory.engine.messaging.OrderLifecyclePayload;
import com.inventory.engine.messaging.OrderLifecycleEventType;
import com.inventory.engine.messaging.OrderLinePayload;
import com.inventory.engine.model.Order;
import com.inventory.engine.model.OrderLine;
import com.inventory.engine.model.OrderStatus;
import com.inventory.engine.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final StockService stockService;
    private final OrderLifecycleEventPublisher orderLifecycleEventPublisher;

    public OrderService(
            OrderRepository repository,
            StockService stockService,
            OrderLifecycleEventPublisher orderLifecycleEventPublisher
    ) {
        this.repository = repository;
        this.stockService = stockService;
        this.orderLifecycleEventPublisher = orderLifecycleEventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        List<OrderLine> lines = request.getLines().stream()
                .map(l -> new OrderLine(l.getProductId(), l.getQuantity()))
                .toList();

        Order order = new Order(lines);
        Order savedOrder = repository.save(order);
        publishLifecycleEvent(savedOrder, OrderLifecycleEventType.ORDER_CREATED);

        return OrderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long orderId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders() {
        return OrderMapper.toResponseList(repository.findAll());
    }

    @Transactional
    public OrderResponse reserve(Long orderId, Long warehouseId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        stockService.reserveOrderAtomically(warehouseId, order);
        Order savedOrder = repository.save(order);
        publishLifecycleEvent(savedOrder, OrderLifecycleEventType.ORDER_RESERVED);

        return OrderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse releaseReservation(Long orderId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Long warehouseId = getReservationWarehouseId(order);
        stockService.releaseOrderReservation(warehouseId, order);
        Order savedOrder = repository.save(order);
        publishLifecycleEvent(savedOrder, OrderLifecycleEventType.ORDER_RELEASED);

        return OrderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.RESERVED) {
            Long warehouseId = getReservationWarehouseId(order);
            stockService.releaseOrderReservation(warehouseId, order);
        }

        order.cancel();
        Order savedOrder = repository.save(order);
        publishLifecycleEvent(savedOrder, OrderLifecycleEventType.ORDER_CANCELLED);

        return OrderMapper.toResponse(savedOrder);
    }

    private Long getReservationWarehouseId(Order order) {
        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new IllegalStateException("order must be in RESERVED status to use reservation warehouse");
        }
        if (order.getWarehouseId() == null) {
            throw new IllegalStateException("reserved order must have a reservation warehouse");
        }
        return order.getWarehouseId();
    }

    private void publishLifecycleEvent(Order order, OrderLifecycleEventType eventType) {
        List<OrderLinePayload> lines = order.getItems().stream()
                .map(line -> new OrderLinePayload(line.getProductId(), line.getQuantity()))
                .toList();

        OrderLifecyclePayload payload = new OrderLifecyclePayload(
                order.getId(),
                order.getStatus().name(),
                order.getWarehouseId(),
                lines
        );

        OrderLifecycleEvent event = new OrderLifecycleEvent(
                UUID.randomUUID(),
                order.getId(),
                UUID.randomUUID().toString(),
                Instant.now(),
                eventType,
                payload
        );

        orderLifecycleEventPublisher.publish(event);
    }
}
