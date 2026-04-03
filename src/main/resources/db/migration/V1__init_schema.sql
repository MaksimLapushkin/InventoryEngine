CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          sku VARCHAR(255) NOT NULL UNIQUE,
                          name VARCHAR(255) NOT NULL,
                          unit VARCHAR(20) NOT NULL,
                          CONSTRAINT chk_products_unit
                              CHECK (unit IN ('PIECE', 'KG', 'LITER'))
);

CREATE TABLE warehouses (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL
);

CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        status VARCHAR(20) NOT NULL,
                        CONSTRAINT chk_orders_status
                            CHECK (status IN ('NEW', 'RESERVED', 'CONFIRMED', 'CANCELLED'))
);

CREATE TABLE order_lines (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INTEGER NOT NULL,
                             CONSTRAINT fk_order_lines_order
                                 FOREIGN KEY (order_id) REFERENCES orders(id),
                             CONSTRAINT fk_order_lines_product
                                 FOREIGN KEY (product_id) REFERENCES products(id),
                             CONSTRAINT chk_order_lines_quantity
                                 CHECK (quantity > 0)
);

CREATE TABLE stock_items (
                             product_id BIGINT NOT NULL,
                             warehouse_id BIGINT NOT NULL,
                             available INTEGER NOT NULL,
                             reserved INTEGER NOT NULL,
                             version BIGINT NOT NULL DEFAULT 0,
                             CONSTRAINT pk_stock_items
                                 PRIMARY KEY (product_id, warehouse_id),
                             CONSTRAINT fk_stock_items_product
                                 FOREIGN KEY (product_id) REFERENCES products(id),
                             CONSTRAINT fk_stock_items_warehouse
                                 FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
                             CONSTRAINT chk_stock_items_available
                                 CHECK (available >= 0),
                             CONSTRAINT chk_stock_items_reserved
                                 CHECK (reserved >= 0)
);

CREATE INDEX idx_order_lines_order_id ON order_lines(order_id);
CREATE INDEX idx_order_lines_product_id ON order_lines(product_id);
CREATE INDEX idx_stock_items_warehouse_id ON stock_items(warehouse_id);