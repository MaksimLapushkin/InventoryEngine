# InventoryEngine

InventoryEngine is a Spring Boot backend service that manages products, warehouses, stock, and orders, with a focus on **atomic stock reservation** and **event-driven architecture**.

The project is designed to demonstrate production-style backend concepts.

---

## Core responsibilities

The service handles:

- product management
- warehouse management
- stock tracking (available / reserved)
- order creation
- atomic stock reservation across multiple order lines
- order cancellation and stock release

All critical operations are transactional and consistent.

---

## Key backend concepts demonstrated

### 1. Atomic stock reservation

Orders may contain multiple lines.

Reservation logic guarantees:

- either **all items are reserved**
- or **nothing is reserved**

This is implemented using:

- database-level conditional updates
- transactional boundaries
- deterministic processing order

---

### 2. Concurrency handling

The system is designed to behave correctly under concurrent requests:

- prevents overselling
- uses safe update patterns
- applies optimistic locking where appropriate

---

### 3. Transactional outbox pattern

The service uses an **outbox pattern** to reliably publish events to Kafka.

**Problem:**

> DB commit and Kafka publish are not atomic.

**Solution:**

- domain changes and event creation are stored in `outbox_event` in the same transaction
- a scheduled publisher reads `NEW` events
- sends them to Kafka
- marks them as `PUBLISHED`

This ensures:

- no lost events
- no inconsistent state between DB and Kafka

---

### 4. Event-driven architecture

The service publishes order lifecycle events to Kafka topic:

```text
order.lifecycle.v1
```

Events include:

- `ORDER_CREATED`
- `ORDER_RESERVED`
- `ORDER_RELEASED`
- `ORDER_CANCELLED`

These events are consumed by a separate service:

👉 `audit-projection-service`

---

### 5. Separation of concerns (Command vs Read)

The system is split into:

- **InventoryEngine** → write-side / command-side  
- **audit-projection-service** → read-side / projection  

InventoryEngine:

- owns transactional logic
- modifies domain state
- publishes events

---

## Architecture overview

```text
Client
  |
  v
InventoryEngine (write-side / command-side)
  |
  | 1. transactional domain changes
  v
PostgreSQL
  |
  | 2. outbox_event stored in the same transaction
  v
Outbox Publisher
  |
  | 3. publishes lifecycle events
  v
Kafka topic: order.lifecycle.v1
  |
  | 4. consumed asynchronously
  v
audit-projection-service (read-side / projection-side)
  |
  +--> order_timeline
  +--> order_view
  +--> read-only REST API
```
InventoryEngine owns the transactional business logic and domain state.
It persists domain changes and outbox records in the same transaction, then publishes lifecycle events to Kafka.

---

## Main entities

- Product
- Warehouse
- StockItem (composite key)
- Order
- OrderLine
- OutboxEvent

---

## API documentation

Interactive API documentation is available via Swagger UI after local startup.

### Swagger UI

http://localhost:8080/swagger-ui/index.html

Use Swagger to:

- inspect request bodies  
- explore response schemas  
- understand endpoint structure and parameters  
<img width="1776" height="788" alt="image" src="https://github.com/user-attachments/assets/1d701f37-8b8d-4674-afcd-798800823b88" />
<img width="1737" height="576" alt="image" src="https://github.com/user-attachments/assets/244fefbe-4970-4079-979a-b411d32cf087" />



---

## Demo flow

A minimal end-to-end scenario:

1. Create a warehouse  
2. Create a product  
3. Add stock to the warehouse  
4. Create an order  
5. Reserve the order  
6. Verify updated stock and order status  

<details>
<summary>Copy-paste demo (PowerShell)</summary>

```powershell
$base = "http://localhost:8080"

# 1. Create warehouse
$warehouseBody = '{"name":"Main Warehouse"}'

$warehouse = Invoke-RestMethod `
  -Method POST `
  -Uri "$base/api/warehouses" `
  -ContentType "application/json" `
  -Body $warehouseBody

$warehouseId = $warehouse.id
$warehouse | ConvertTo-Json -Depth 5

# 2. Create product
$productBody = '{"sku":"SKU-001","name":"Milk","unit":"PIECE"}'

$product = Invoke-RestMethod `
  -Method POST `
  -Uri "$base/api/products" `
  -ContentType "application/json" `
  -Body $productBody

$productId = $product.id
$product | ConvertTo-Json -Depth 5

# 3. Add stock
$addStockBody = "{`"productId`":$productId,`"warehouseId`":$warehouseId,`"quantity`":10}"

Invoke-RestMethod `
  -Method POST `
  -Uri "$base/api/stocks/add" `
  -ContentType "application/json" `
  -Body $addStockBody

# 4. Check stock before reservation
$stockBefore = Invoke-RestMethod `
  -Method GET `
  -Uri "$base/api/stocks?productId=$productId&warehouseId=$warehouseId"

$stockBefore | ConvertTo-Json -Depth 5

# 5. Create order
$orderBody = "{`"lines`":[{`"productId`":$productId,`"quantity`":3}]}"

$order = Invoke-RestMethod `
  -Method POST `
  -Uri "$base/api/orders" `
  -ContentType "application/json" `
  -Body $orderBody

$orderId = $order.id
$order | ConvertTo-Json -Depth 5

# 6. Reserve order
$reservedOrder = Invoke-RestMethod `
  -Method POST `
  -Uri "$base/api/orders/$orderId/reserve?warehouseId=$warehouseId"

$reservedOrder | ConvertTo-Json -Depth 5

# 7. Check order after reservation
$orderAfter = Invoke-RestMethod `
  -Method GET `
  -Uri "$base/api/orders/$orderId"

$orderAfter | ConvertTo-Json -Depth 5

# 8. Check stock after reservation
$stockAfter = Invoke-RestMethod `
  -Method GET `
  -Uri "$base/api/stocks?productId=$productId&warehouseId=$warehouseId"

$stockAfter | ConvertTo-Json -Depth 5
```

</details>

<img width="1208" height="928" alt="image" src="https://github.com/user-attachments/assets/9abada25-c297-4e65-8086-d00dadee9392" />

---

## Expected result

- stock is reserved atomically  
- lifecycle events are written to outbox  
- events are published to Kafka  
- projection service updates read model  

---

## Example flow

1. Create warehouse  
2. Create product  
3. Add stock  
4. Create order  
5. Reserve order  

**Result:**

- stock is reserved atomically
- lifecycle events are written to outbox
- events are published to Kafka
- projection service updates read model

---

## Tech stack

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- Kafka
- Maven

---

## Why this project is relevant

This project demonstrates:

- transactional consistency
- concurrency-safe stock reservation
- event-driven backend design
- outbox pattern implementation
- separation of command-side and read-side

It is designed to reflect real backend system behavior rather than simplified academic examples.

---

## Related service

This project is designed to work together with:

* [audit-projection-service](https://github.com/MaksimLapushkin/audit-projection-service)

That service consumes Kafka order lifecycle events from `InventoryEngine` and builds:

* append-only audit history
* current order state projection
* read-only endpoints for querying order timeline and latest status

Together, the two repositories demonstrate a simple event-driven split between:

* write-side / command-side logic in `InventoryEngine`
* read-side / projection-side logic in `audit-projection-service`

---

## Testing

The project includes tests focused on correctness of transactional and concurrency-sensitive logic.

Testing focus includes:

* reservation correctness
* atomic multi-line behavior
* concurrency-safe stock updates
* integration coverage for backend flows
<img width="1599" height="361" alt="image" src="https://github.com/user-attachments/assets/7efcb4a4-c7eb-44f3-be09-d273f9bd4e57" />



## Author

Maksim Lapushkin
