ALTER TABLE orders ADD COLUMN customer_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN delivery_address VARCHAR(255);
ALTER TABLE orders ADD COLUMN delivery_city VARCHAR(255);
ALTER TABLE orders ADD COLUMN delivery_postal_code VARCHAR(255);
ALTER TABLE orders ADD COLUMN customer_phone VARCHAR(255);

UPDATE orders
SET customer_name = 'Unknown Customer'
WHERE customer_name IS NULL;

UPDATE orders
SET delivery_address = 'Unknown Address'
WHERE delivery_address IS NULL;

UPDATE orders
SET delivery_city = 'Unknown City'
WHERE delivery_city IS NULL;

UPDATE orders
SET delivery_postal_code = 'Unknown Postal Code'
WHERE delivery_postal_code IS NULL;

UPDATE orders
SET customer_phone = 'Unknown Phone'
WHERE customer_phone IS NULL;

ALTER TABLE orders ALTER COLUMN customer_name SET NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_address SET NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_city SET NOT NULL;
ALTER TABLE orders ALTER COLUMN delivery_postal_code SET NOT NULL;
ALTER TABLE orders ALTER COLUMN customer_phone SET NOT NULL;
