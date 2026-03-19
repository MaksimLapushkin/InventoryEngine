# InventoryEngine

InventoryEngine is a backend-style Java project focused on practicing core backend engineering concepts such as com.inventory.engine.service-layer architecture, data consistency, and inventory management logic.

The system simulates a warehouse where products can be stored, reserved for orders, and tracked through stock records with atomic operations.

![CI](https://github.com/MaksimLapushkin/InventoryEngine/actions/workflows/maven.yml/badge.svg)

Project evolution

Phase 1:
Core Java implementation (in-memory repositories, service layer, atomic operations)

Phase 2:
Spring Boot migration (REST API, JPA, database integration)

---


## Features

- Product management
- Warehouse stock tracking
- Order creation
- Atomic stock reservation (no partial updates)
- Prevention of inconsistent states
- Service–Repository architecture
- In-memory data storage
- File report generation
- Usage of Optional, Streams, and Collections

---

## Architecture

The project follows a layered backend structure:

com.inventory.engine.model → domain entities  
com.inventory.engine.repository → data access layer (in-memory)  
com.inventory.engine.service → business logic  
com.inventory.engine.util → reporting utilities  
app → application entry point  

Example flow:

Order → StockService → StockRepository → StockItem

---

## Project Structure

src/main/java

com.inventory.engine.model/
Product  
Order  
OrderLine  
OrderStatus  
StockItem  
StockKey  
Warehouse  

com.inventory.engine.repository/
ProductRepository  
StockRepository  
InMemoryProductRepository  
InMemoryStockRepository  

com.inventory.engine.service/
ProductService  
StockService  

com.inventory.engine.util/
InventoryReportService  

com.inventory.engine.InventoryEngineApplication.java

---

## Example Scenario

1. Products are created  
2. Stock is added to a warehouse  
3. An order is created with multiple items  
4. The system checks stock availability  
5. If all items are available → reservation happens atomically  

This guarantees:
- No partial reservations  
- Consistent stock state  

---

## Example Output

Order status: RESERVED

Product 1 available=18 reserved=2  
Product 2 available=15 reserved=0  
Product 3 available=27 reserved=3  

---

## Testing

The project includes unit tests for core business logic:

- Stock reservation
- Stock release
- Edge cases (not enough stock, missing stock)
- Data consistency validation

JUnit 5 + AssertJ

---

## CI (GitHub Actions)

Runs on every push  
Builds project with Maven  
Executes tests automatically  

---

## Docker

docker build -t inventory-engine .  
docker run inventory-engine  

---

## How to run

mvn clean package  
java -jar target/InventoryEngine-1.0-SNAPSHOT.jar  

---

## Technologies

- Java 21
- Maven
- JUnit 5
- AssertJ
- Java Collections
- Stream API
- Optional
- File I/O
- Basic concurrency reasoning

---

## Purpose

This project was created to practice:

- Backend architecture design
- Service-layer logic
- Atomic operations and data consistency
- Clean code and structure
- Test-driven thinking

---

## Future Improvements

- REST API with Spring Boot
- PostgreSQL + JPA/Hibernate
- DTO layer
- Validation
- Transaction management
- Integration testing
- Docker Compose setup
