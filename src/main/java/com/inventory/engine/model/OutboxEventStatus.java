package com.inventory.engine.model;

public enum OutboxEventStatus {
    NEW,
    IN_PROGRESS,
    PUBLISHED,
    FAILED
}
