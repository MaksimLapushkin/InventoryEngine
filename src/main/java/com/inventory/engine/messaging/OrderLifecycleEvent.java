package com.inventory.engine.messaging;

import java.time.Instant;
import java.util.List;

public record OrderLifecycleEvent(
        OrderLifecycleEventType eventType,
        Long orderId,
        String status,
        Long warehouseId,
        Instant occurredAt,
        List<Line> lines
) {
    public OrderLifecycleEvent {
        lines = List.copyOf(lines);
    }

    public record Line(Long productId, int quantity) {
    }
}
