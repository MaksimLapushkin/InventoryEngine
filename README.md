# InventoryEngine

InventoryEngine is a Spring Boot backend service that manages products, warehouses, stock, and orders, with a focus on **atomic stock reservation** and **event-driven architecture**.

The project is designed to demonstrate production-style backend concepts rather than simple CRUD logic.

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
Client → InventoryEngine (command-side)
               |
               | transactional changes
               v
         PostgreSQL
               |
               | outbox_event
               v
        Outbox Publisher
               |
               v
             Kafka
               |
               v
audit-projection-service (read-side)
```

---

## Main entities

- Product
- Warehouse
- StockItem (composite key)
- Order
- OrderLine
- OutboxEvent

---

## API examples

### Create warehouse

```http
POST /api/warehouses
```

### Create product

```http
POST /api/products
```

### Add stock

```http
POST /api/stocks/add
```

### Create order

```http
POST /api/orders
```

### Reserve order

```http
POST /api/orders/{id}/reserve?warehouseId=1
```

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

- audit-projection-service  
  consumes Kafka events and builds audit timeline and read model

---

## Author

Maksim Lapushkin
