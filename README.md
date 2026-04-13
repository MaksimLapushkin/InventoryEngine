# InventoryEngine

InventoryEngine is a Spring Boot backend service that manages products, warehouses, stock, and orders, with a focus on **atomic stock reservation** and **event-driven architecture**.

---

## Architecture

The system follows a command-side / read-side separation:

- **InventoryEngine** → write-side / command-side  
- **audit-projection-service** → read-side / projection-side  

InventoryEngine owns transactional domain logic and publishes lifecycle events to Kafka.

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

## Key concepts

### Atomic stock reservation

Orders may contain multiple lines.

Reservation guarantees:

- either **all items are reserved**
- or **nothing is reserved**

Implemented using:

- database-level conditional updates
- transactional boundaries
- deterministic processing order

---

### Concurrency handling

The system is designed to behave correctly under concurrent requests:

- prevents overselling
- uses safe update patterns
- applies optimistic locking where appropriate

---

### Transactional outbox pattern

Problem:

> database commit and Kafka publish are not atomic

Solution:

- domain changes and events are stored in `outbox_event` in the same transaction
- background publisher reads `NEW` events
- publishes to Kafka
- marks them as `PUBLISHED`

---

### Event-driven architecture

The service publishes events to:

```text
order.lifecycle.v1
```

Events include:

- `ORDER_CREATED`
- `ORDER_RESERVED`
- `ORDER_FULFILLED`
- `ORDER_RELEASED`
- `ORDER_CANCELLED`

These events are consumed by:

👉 audit-projection-service

---

## Main entities

- Product  
- Warehouse  
- StockItem (composite key)  
- Order  
- OrderLine  
- OutboxEvent  

---

## REST API

Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

Use it to explore endpoints and payloads.

<img width="1679" height="755" alt="image" src="https://github.com/user-attachments/assets/2567ac54-c6b1-4471-8056-6a3d2fff76d3" />
<img width="1660" height="580" alt="image" src="https://github.com/user-attachments/assets/8e2b91aa-b8c6-439e-a20e-2b358bb4e56a" />

---

## How to run

### Prerequisites

- Java 21  
- Maven  
- Docker & Docker Compose  

---

### 1. Clone repository

```bash
git clone https://github.com/MaksimLapushkin/InventoryEngine.git
cd InventoryEngine
```

---

### 2. Start infrastructure

```bash
docker-compose up -d
```

---

### 3. Run application

```bash
mvn spring-boot:run
```

Or:

```bash
java -jar target/InventoryEngine-*.jar
```

---

### 4. Access

- API: http://localhost:8080  
- Swagger: http://localhost:8080/swagger-ui/index.html  

---

## Demo flow

1. Create warehouse  
2. Create product  
3. Add stock  
4. Create order  
5. Reserve order  
6. Fulfill order  

<details>
<summary>PowerShell demo</summary>

```powershell
$base = "http://localhost:8080"

# Create warehouse
$warehouse = Invoke-RestMethod -Method POST -Uri "$base/api/warehouses" `
  -ContentType "application/json" -Body '{"name":"Main Warehouse"}'

$warehouseId = $warehouse.id

# Create product
$product = Invoke-RestMethod -Method POST -Uri "$base/api/products" `
  -ContentType "application/json" -Body '{"sku":"SKU-001","name":"Milk","unit":"PIECE"}'

$productId = $product.id

# Add stock
Invoke-RestMethod -Method POST -Uri "$base/api/stocks/add" `
  -ContentType "application/json" `
  -Body "{`"productId`":$productId,`"warehouseId`":$warehouseId,`"quantity`":10}"

# Create order
$order = Invoke-RestMethod -Method POST -Uri "$base/api/orders" `
  -ContentType "application/json" `
  -Body "{`"lines`":[{`"productId`":$productId,`"quantity`":3}]}"

$orderId = $order.id

# Reserve
Invoke-RestMethod -Method POST -Uri "$base/api/orders/$orderId/reserve?warehouseId=$warehouseId"

# Fulfill
Invoke-RestMethod -Method POST -Uri "$base/api/orders/$orderId/fulfill"
```

</details>

<img width="1208" height="928" alt="image" src="https://github.com/user-attachments/assets/9abada25-c297-4e65-8086-d00dadee9392" />

---

## Expected result

- stock is reserved atomically  
- stock is reduced after fulfillment  
- events are written to outbox  
- events are published to Kafka  
- projection service updates read model  

---

## Testing

Tests focus on:

- atomic reservation correctness  
- multi-line consistency  
- concurrency safety  
- integration flows  

<img width="1599" height="361" alt="image" src="https://github.com/user-attachments/assets/7efcb4a4-c7eb-44f3-be09-d273f9bd4e57" />

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

## Related service

- [audit-projection-service](https://github.com/MaksimLapushkin/audit-projection-service)

Consumes lifecycle events and builds:

- audit history  
- current state projection  

---

## Author

Maksim Lapushkin
