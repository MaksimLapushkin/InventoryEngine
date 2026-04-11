ALTER TABLE outbox_event
    ADD COLUMN attempt_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE outbox_event
    ADD COLUMN next_attempt_at TIMESTAMP(6) WITH TIME ZONE;

CREATE INDEX idx_outbox_event_retryable
    ON outbox_event(status, next_attempt_at, created_at);
