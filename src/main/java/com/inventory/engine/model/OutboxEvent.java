package com.inventory.engine.model;

import com.inventory.engine.messaging.OrderLifecycleEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_event",
        uniqueConstraints = @UniqueConstraint(name = "uk_outbox_event_event_id", columnNames = "event_id")
)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    protected OutboxEvent() {
    }

    private OutboxEvent(
            String aggregateType,
            String aggregateId,
            String eventType,
            UUID eventId,
            String correlationId,
            String payloadJson
    ) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventId = eventId;
        this.correlationId = correlationId;
        this.payloadJson = payloadJson;
        this.status = OutboxEventStatus.NEW;
    }

    public static OutboxEvent forOrderLifecycleEvent(OrderLifecycleEvent event, String payloadJson) {
        return new OutboxEvent(
                "ORDER",
                event.aggregateId().toString(),
                event.eventType(),
                event.eventId(),
                event.correlationId(),
                payloadJson
        );
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = OutboxEventStatus.NEW;
        }
    }

    public Long getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public OutboxEventStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void markPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = OutboxEventStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
