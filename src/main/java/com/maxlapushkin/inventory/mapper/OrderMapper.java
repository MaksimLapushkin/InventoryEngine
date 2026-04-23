package com.maxlapushkin.inventory.mapper;

import com.maxlapushkin.inventory.dto.OrderLineResponse;
import com.maxlapushkin.inventory.dto.OrderResponse;
import com.maxlapushkin.inventory.model.Order;
import com.maxlapushkin.inventory.model.OrderLine;

import java.util.List;

public class OrderMapper {

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                toLineResponses(order.getItems()),
                order.getCustomerName(),
                order.getDeliveryAddress(),
                order.getDeliveryCity(),
                order.getDeliveryPostalCode(),
                order.getCustomerPhone(),
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
