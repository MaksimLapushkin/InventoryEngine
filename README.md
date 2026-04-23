# InventoryEngine

InventoryEngine is a Spring Boot backend service that manages products, warehouses, stock, and orders, with a focus on **atomic stock reservation**, **transactional consistency**, and **event-driven integration**.

---

## Architecture

The system follows a service-oriented event-driven design:

* **InventoryEngine** -> write-side / command-side for inventory and order domain logic
* **delivery-service** -> consumes `ORDER_FULFILLED` and manages delivery lifecycle
* **audit-projection-service** -> consumes order and delivery events to build read models

InventoryEngine owns transactional order and stock logic and publishes lifecycle events to Kafka.

---

## Core responsibilities

The service handles:

* product management
* warehouse management
* stock tracking (`available` / `reserved`)
* order creation
* capture of delivery/customer data at order creation time
* atomic stock reservation across multiple order lines
* order reservation, fulfillment, cancellation, and release flows
* publishing order lifecycle events to Kafka

All critical operations are transactional and consistent.

---

## Key concepts

### Atomic stock reservation

Orders may contain multiple lines.

Reservation guarantees:

* either **all items are reserved**
* or **nothing is reserved**

Implemented using:

* database-level conditional updates
* transactional boundaries
* deterministic processing order

---

### Concurrency handling

The system is designed to behave correctly under concurrent requests:

* prevents overselling
* uses safe update patterns
* applies optimistic locking where appropriate

---

### Transactional outbox pattern

Problem:

> database commit and Kafka publish are not atomic

Solution:

* domain changes and events are stored in `outbox_event` in the same transaction
* background publisher reads pending events
* publishes them to Kafka
* marks them as published after successful delivery

This prevents lost events and keeps integration reliable.

---

### Event-driven architecture

The service publishes events to:

```text
order.lifecycle.v1
```

Events include:

* `ORDER_CREATED`
* `ORDER_RESERVED`
* `ORDER_FULFILLED`
* `ORDER_RELEASED`
* `ORDER_CANCELLED`

`ORDER_FULFILLED` includes a delivery/customer snapshot so downstream services can continue the workflow.

These events are consumed by:

* **delivery-service**
* **audit-projection-service**

---

## Main entities

* Product
* Warehouse
* StockItem (composite key)
* Order
* OrderLine
* OutboxEvent

---

## REST API

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Use it to explore endpoints and payloads.

<img width="1715" height="768" alt="image" src="https://github.com/user-attachments/assets/38d6cd71-8f62-484e-b24c-ff12e86e939a" />

---

## How to run

### Prerequisites

* Java 21
* Maven
* Docker & Docker Compose

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

This starts:

* PostgreSQL
* Zookeeper
* Kafka
* Kafka UI

The PostgreSQL init script creates the required databases automatically on first startup.

---

### 3. Run application

```bash
mvn spring-boot:run
```

---

### 4. Access

* API: `http://localhost:8080`
* Swagger: `http://localhost:8080/swagger-ui/index.html`
* Kafka UI: `http://localhost:8081`

---

## Demo flow

A minimal end-to-end scenario:

1. Create a warehouse
2. Create a product
3. Add stock
4. Create an order with delivery data
5. Reserve the order
6. Fulfill the order
7. Verify updated stock and order status
8. Verify that `ORDER_FULFILLED` is published with delivery snapshot
9. Verify downstream updates in delivery-service and audit-projection-service

<details>
<summary>PowerShell demo</summary>

```powershell
$base = "http://localhost:8080"

$warehouse = Invoke-RestMethod -Method POST -Uri "$base/api/warehouses" -ContentType "application/json" -Body '{"name":"Main Warehouse"}'
$warehouse

$warehouseId = $warehouse.id

$product = Invoke-RestMethod -Method POST -Uri "$base/api/products" -ContentType "application/json" -Body '{"sku":"SKU-001","name":"Milk","unit":"PIECE"}'
$product

$productId = $product.id

$stockAdded = Invoke-RestMethod -Method POST -Uri "$base/api/stocks/add" -ContentType "application/json" -Body "{`"productId`":$productId,`"warehouseId`":$warehouseId,`"quantity`":10}"
$stockAdded

$orderBody = @"
{
  "customerName": "Max Lapushkin",
  "deliveryAddress": "Vinohradska 123",
  "deliveryCity": "Prague",
  "deliveryPostalCode": "13000",
  "customerPhone": "+420123456789",
  "lines": [
    {
      "productId": $productId,
      "quantity": 3
    }
  ]
}
"@

$order = Invoke-RestMethod -Method POST -Uri "$base/api/orders" -ContentType "application/json" -Body $orderBody
$order

$orderId = $order.id

$orderAfterCreate = Invoke-RestMethod -Method GET -Uri "$base/api/orders/$orderId"
$orderAfterCreate

$reservedOrder = Invoke-RestMethod -Method POST -Uri "$base/api/orders/$orderId/reserve?warehouseId=$warehouseId"
$reservedOrder

$orderAfterReserve = Invoke-RestMethod -Method GET -Uri "$base/api/orders/$orderId"
$orderAfterReserve

$stockAfterReserve = Invoke-RestMethod -Method GET -Uri "$base/api/stocks?productId=$productId&warehouseId=$warehouseId"
$stockAfterReserve

$fulfilledOrder = Invoke-RestMethod -Method POST -Uri "$base/api/orders/$orderId/fulfill"
$fulfilledOrder

$orderAfterFulfill = Invoke-RestMethod -Method GET -Uri "$base/api/orders/$orderId"
$orderAfterFulfill

$stockAfterFulfill = Invoke-RestMethod -Method GET -Uri "$base/api/stocks?productId=$productId&warehouseId=$warehouseId"
$stockAfterFulfill
```

</details>

<img width="1208" height="928" alt="image" src="https://github.com/user-attachments/assets/9abada25-c297-4e65-8086-d00dadee9392" />

---

## Expected result

* stock is reserved atomically
* stock is reduced after fulfillment
* events are written to outbox
* events are published to Kafka
* `ORDER_FULFILLED` includes delivery/customer data
* delivery-service can create a delivery from the event
* audit-projection-service updates the read model

---

## Testing

Tests focus on:

* atomic reservation correctness
* multi-line consistency
* concurrency safety
* integration flows
* event publishing consistency

<img width="1599" height="361" alt="image" src="https://github.com/user-attachments/assets/7efcb4a4-c7eb-44f3-be09-d273f9bd4e57" />

---

## Tech stack

* Java 21
* Spring Boot
* Spring Data JPA
* Hibernate
* PostgreSQL
* Flyway
* Kafka
* Maven

---

## Related services

* [delivery-service](https://github.com/MaksimLapushkin/delivery-serivce)
* [audit-projection-service](https://github.com/MaksimLapushkin/audit-projection-service)

### delivery-service

Consumes `ORDER_FULFILLED` and manages:

* delivery creation
* delivery lifecycle transitions
* delivery timeline
* delivery outbox publishing

### audit-projection-service

Consumes order and delivery events and builds:

* audit history
* current state projection

---

## Author

Maksim Lapushkin
