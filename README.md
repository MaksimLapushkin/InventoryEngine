# InventoryEngine

InventoryEngine is a small backend-style project written in Java to practice core backend concepts such as service-layer architecture, data consistency, and inventory management logic.

The project simulates a simple warehouse system where products can be stored, reserved for orders, and tracked through stock records.

This repository focuses on **Java Core backend fundamentals**.


## Features

- Product management
- Warehouse stock tracking
- Order creation
- Atomic stock reservation for orders
- Prevention of partial updates during reservation
- Service–Repository architecture
- File report generation
- Use of Optional, Streams, and collections


## Architecture

The project follows a simplified backend architecture:

model → domain entities  
repository → data storage layer (in-memory)  
service → business logic  
util → reporting utilities  
app → application entry point  

Example flow:
Order → StockService → StockRepository → StockItem

## Project Structure

src/main/java

model
  Product
  Order
  OrderLine
  OrderStatus
  StockItem
  StockKey
  Warehouse

repository
  ProductRepository
  StockRepository
  InMemoryProductRepository
  InMemoryStockRepository

service
  ProductService
  StockService

util
  InventoryReportService


Main


## Example Scenario

The application demonstrates a simple order reservation flow:

1. Products are created  
2. Stock is added to a warehouse  
3. An order is created with multiple items  
4. The system checks stock availability  
5. If all items are available, the order is reserved atomically  

This prevents partial reservation states.

## Example Output

Order status: RESERVED

Product 1 available=18 reserved=2
Product 2 available=15 reserved=0
Product 3 available=27 reserved=3

## Technologies

- Java
- Java Collections
- Stream API
- Optional
- File I/Oс
- Basic concurrency considerations

## Purpose

This project was created to practice backend engineering fundamentals including:

- Object-oriented design
- Service-layer architecture
- Data consistency and atomic operations
- Separation of concerns
- Clean project structure

## Future Improvements

Possible next steps:

- REST API with Spring Boot
- PostgreSQL integration with JPA/Hibernate
- DTO layer
- Validation
- Unit testing with JUnit
- Transaction management
