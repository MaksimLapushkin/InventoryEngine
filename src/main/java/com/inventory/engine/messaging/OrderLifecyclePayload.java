package com.inventory.engine.messaging;

import java.util.List;
import java.util.Objects;

public record OrderLifecyclePayload(
        Long orderId,
        String status,
        Long warehouseId,
        List<OrderLinePayload> lines
) {
    public OrderLifecyclePayload {
        orderId = Objects.requireNonNull(orderId, "orderId must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines must not be null"));
    }
}
