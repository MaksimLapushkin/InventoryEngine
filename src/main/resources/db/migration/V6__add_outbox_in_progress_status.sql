ALTER TABLE outbox_event DROP CONSTRAINT IF EXISTS chk_outbox_event_status;

ALTER TABLE outbox_event
    ADD CONSTRAINT chk_outbox_event_status
        CHECK (status IN ('NEW', 'IN_PROGRESS', 'PUBLISHED', 'FAILED'));
