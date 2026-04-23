package com.maxlapushkin.inventory.messaging;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OrderLifecycleEvent(
        UUID eventId,
        Long aggregateId,
        String correlationId,
        Instant occurredAt,
        String eventType,
        OrderLifecyclePayload payload
) {
    public OrderLifecycleEvent {
        eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        aggregateId = Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        payload = Objects.requireNonNull(payload, "payload must not be null");
    }

    public OrderLifecycleEvent(
            UUID eventId,
            Long aggregateId,
            String correlationId,
            Instant occurredAt,
            OrderLifecycleEventType eventType,
            OrderLifecyclePayload payload
    ) {
        this(eventId, aggregateId, correlationId, occurredAt, eventType.name(), payload);
    }

    public OrderLifecycleEventType eventTypeEnum() {
        return OrderLifecycleEventType.valueOf(eventType);
    }
}
