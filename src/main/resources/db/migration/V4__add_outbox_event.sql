CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_id UUID NOT NULL,
    correlation_id VARCHAR(100),
    payload_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP(6) WITH TIME ZONE,
    error_message TEXT,
    CONSTRAINT uk_outbox_event_event_id UNIQUE (event_id),
    CONSTRAINT chk_outbox_event_status
        CHECK (status IN ('NEW', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_outbox_event_status_created_at
    ON outbox_event(status, created_at);

CREATE INDEX idx_outbox_event_aggregate
    ON outbox_event(aggregate_type, aggregate_id);
