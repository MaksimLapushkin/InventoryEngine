UPDATE orders
SET status = 'CREATED'
WHERE status = 'NEW';

UPDATE orders
SET status = 'RESERVED'
WHERE status = 'CONFIRMED';

ALTER TABLE orders DROP CONSTRAINT IF EXISTS chk_orders_status;

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status
        CHECK (status IN ('CREATED', 'RESERVED', 'CANCELLED', 'FAILED'));
