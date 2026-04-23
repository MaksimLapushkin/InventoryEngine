package com.maxlapushkin.inventory.messaging;

import java.util.List;
import java.util.Objects;

public record OrderLifecyclePayload(
        Long orderId,
        String status,
        Long warehouseId,
        String customerName,
        String deliveryAddress,
        String deliveryCity,
        String deliveryPostalCode,
        String customerPhone,
        List<OrderLinePayload> lines
) {
    public OrderLifecyclePayload {
        orderId = Objects.requireNonNull(orderId, "orderId must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
        customerName = Objects.requireNonNull(customerName, "customerName must not be null");
        deliveryAddress = Objects.requireNonNull(deliveryAddress, "deliveryAddress must not be null");
        deliveryCity = Objects.requireNonNull(deliveryCity, "deliveryCity must not be null");
        deliveryPostalCode = Objects.requireNonNull(deliveryPostalCode, "deliveryPostalCode must not be null");
        customerPhone = Objects.requireNonNull(customerPhone, "customerPhone must not be null");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines must not be null"));
    }
}
