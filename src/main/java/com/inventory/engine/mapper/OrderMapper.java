package com.inventory.engine.mapper;

import com.inventory.engine.dto.OrderLineResponse;
import com.inventory.engine.dto.OrderResponse;
import com.inventory.engine.model.Order;
import com.inventory.engine.model.OrderLine;

import java.util.List;

public class OrderMapper {

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                toLineResponses(order.getItems()),
                order.getStatus().name(),
                order.getWarehouseId()
        );
    }

    public static List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    private static List<OrderLineResponse> toLineResponses(List<OrderLine> items) {
        return items.stream()
                .map(line -> new OrderLineResponse(line.getProductId(), line.getQuantity()))
                .toList();
    }
}
