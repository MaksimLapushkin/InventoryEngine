ALTER TABLE orders
    ADD COLUMN warehouse_id BIGINT;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses(id);
