package com.maxlapushkin.inventory.model;

public enum OutboxEventStatus {
    NEW,
    IN_PROGRESS,
    PUBLISHED,
    FAILED
}
