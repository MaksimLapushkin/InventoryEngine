# InventoryEngine

Backend-style inventory management API built with Spring Boot.

InventoryEngine models products, warehouses, stock levels, and orders, with a focus on transactional consistency and atomic stock reservation under concurrency.

## What the project does

* manage products and warehouses
* track stock by `(product, warehouse)`
* create orders with multiple order lines
* reserve stock for orders atomically
* release reservations and cancel orders
* expose the domain through a REST API
* document the API with OpenAPI / Swagger UI

## Why this project exists

This project is meant to practice backend engineering topics that matter in real systems:

* layered architecture
* DTO-based REST API design
* validation and error handling
* JPA / Hibernate persistence
* PostgreSQL integration
* transactional consistency
* concurrency-safe stock reservation
* testing across controller, integration, and concurrency scenarios

## Key backend idea: atomic reservation

The main business rule in the project is that order reservation must be all-or-nothing.

Stock reservation is enforced at the database level with conditional updates instead of a naive read-check-write flow. This prevents overselling when concurrent requests try to reserve the same stock.

For multi-line orders, reservation is executed inside a transaction. If one line cannot be reserved, the whole operation rolls back and no partial reservation remains.

## API overview

Main resource groups:

* `Products`
* `Warehouses`
* `Stock`
* `Orders`

Typical flow:

1. Create a product
2. Create a warehouse
3. Add stock to the warehouse
4. Create an order
5. Reserve the order against warehouse stock
6. Inspect resulting order and stock state

## Example endpoints

### Products

* `GET /api/products`
* `GET /api/products/{id}`
* `POST /api/products`

### Warehouses

* `GET /api/warehouses`
* `GET /api/warehouses/{id}`
* `POST /api/warehouses`
* `DELETE /api/warehouses/{id}`

### Stock

* `GET /api/stocks`
* `POST /api/stocks/add`
* `POST /api/stocks/reserve`
* `POST /api/stocks/release`

### Orders

* `GET /api/orders`
* `GET /api/orders/{orderId}`
* `POST /api/orders`
* `POST /api/orders/{orderId}/reserve`
* `POST /api/orders/{orderId}/cancel`

## Tech stack

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Bean Validation
* PostgreSQL
* H2 for tests
* Maven
* OpenAPI / Swagger UI
* Docker + Docker Compose

## Project structure

```text
src/main/java/com/inventory/engine
├── config        # application / OpenAPI config
├── controller    # REST controllers
├── dto           # request / response models
├── exception     # domain and API exceptions
├── mapper        # DTO mappers
├── model         # domain entities
├── repository    # JPA repositories
└── service       # business logic
```

## Running locally

### 1. Start PostgreSQL with Docker Compose

```bash
docker compose up -d
```

### 2. Run the application

```bash
mvn spring-boot:run
```

Or build and run the jar:

```bash
mvn clean package
java -jar target/inventory-engine-0.1.0.jar
```

## API documentation

After the application starts, open:

* `http://localhost:8080/swagger-ui.html`
* `http://localhost:8080/v3/api-docs`

## Testing

The project includes several layers of tests:

* controller tests for the HTTP layer
* integration tests for the REST API
* business logic tests for stock and orders
* concurrency-focused tests for competing reservations
* PostgreSQL-backed tests via Testcontainers

Example scenarios covered:

* invalid request validation
* successful create / reserve flows
* rollback of multi-line order reservation on failure
* two concurrent requests trying to reserve the same stock
* prevention of oversell under concurrent load

## Notes

This repository contains the current Spring Boot + JPA implementation.

Older in-memory repository code is not part of the active implementation anymore. If legacy code exists in history or a separate branch, it is retained only as project evolution, not as the current runtime path.

## Possible next improvements

* Flyway database migrations
* pagination / filtering improvements
* richer API error documentation
* authentication / authorization
* metrics / observability
* Kafka-based integration events

## Author

Maksim Lapushkin

