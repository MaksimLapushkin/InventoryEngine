package com.inventory.engine.service;

import com.inventory.engine.exception.NotEnoughStockException;
import com.inventory.engine.exception.StockNotFoundException;
import com.inventory.engine.model.StockItem;
import com.inventory.engine.model.StockKey;
import com.inventory.engine.model.Unit;
import com.inventory.engine.repository.ProductRepository;
import com.inventory.engine.repository.StockRepository;
import com.inventory.engine.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StockService tests")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @BeforeEach
    void clean() {
        stockRepository.deleteAll();
        productRepository.deleteAll();
        warehouseRepository.deleteAll();
    }

    private Long createProduct() {
        return productService.addProduct(
                "SKU-" + UUID.randomUUID(),
                "Product-" + UUID.randomUUID(),
                Unit.PIECE
        ).getId();
    }

    private Long createWarehouse() {
        return warehouseService.create("WH-" + UUID.randomUUID()).getId();
    }

    @Nested
    @DisplayName("addStock()")
    class AddStockTests {

        @Test
        void shouldIncreaseAvailableStock() {
            Long productId = createProduct();
            Long warehouseId = createWarehouse();

            stockService.addStock(productId, warehouseId, 1);

            StockItem result = stockRepository
                    .findById(new StockKey(productId, warehouseId))
                    .orElseThrow();

            assertThat(result.getProductId()).isEqualTo(productId);
            assertThat(result.getWarehouseId()).isEqualTo(warehouseId);
            assertThat(result.getAvailable()).isEqualTo(1);
            assertThat(result.getReserved()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("reserveStock()")
    class ReserveStockTests {

        @Test
        void shouldReserveStockSuccessfully() {
            Long productId = createProduct();
            Long warehouseId = createWarehouse();

            stockService.addStock(productId, warehouseId, 1);
            stockService.reserveStock(productId, warehouseId, 1);

            StockItem result = stockRepository
                    .findById(new StockKey(productId, warehouseId))
                    .orElseThrow();

            assertThat(result.getAvailable()).isEqualTo(0);
            assertThat(result.getReserved()).isEqualTo(1);
        }

        @Test
        void shouldThrowExceptionWhenStockNotFound() {
            assertThatThrownBy(() -> stockService.reserveStock(999_999L, 999_999L, 999))
                    .isInstanceOf(StockNotFoundException.class);
        }

        @Test
        void shouldThrowExceptionWhenNotEnoughStock() {
            Long productId = createProduct();
            Long warehouseId = createWarehouse();

            stockService.addStock(productId, warehouseId, 1);

            assertThatThrownBy(() -> stockService.reserveStock(productId, warehouseId, 2))
                    .isInstanceOf(NotEnoughStockException.class);
        }
    }

    @Nested
    @DisplayName("releaseStock()")
    class ReleaseStockTests {

        @Test
        void shouldReleaseStockSuccessfully() {
            Long productId = createProduct();
            Long warehouseId = createWarehouse();

            stockService.addStock(productId, warehouseId, 5);
            stockService.reserveStock(productId, warehouseId, 3);
            stockService.releaseStock(productId, warehouseId, 2);

            StockItem result = stockRepository
                    .findById(new StockKey(productId, warehouseId))
                    .orElseThrow();

            assertThat(result.getAvailable()).isEqualTo(4);
            assertThat(result.getReserved()).isEqualTo(1);
        }

        @Test
        void shouldThrowExceptionWhenReleasingMoreThanReserved() {
            Long productId = createProduct();
            Long warehouseId = createWarehouse();

            stockService.addStock(productId, warehouseId, 5);
            stockService.reserveStock(productId, warehouseId, 3);

            assertThatThrownBy(() -> stockService.releaseStock(productId, warehouseId, 4))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot release more items than reserved");
        }
    }

    @Nested
    @DisplayName("Stock Separation")
    class GetAvailableStock {

        @Test
        void shouldKeepStockSeparatedByProductAndWarehouse() {
            Long productA = createProduct();
            Long productB = createProduct();
            Long warehouseA = createWarehouse();
            Long warehouseB = createWarehouse();

            stockService.addStock(productA, warehouseA, 5);
            stockService.addStock(productA, warehouseA, 2);
            stockService.reserveStock(productA, warehouseA, 3);

            stockService.addStock(productB, warehouseA, 5);
            stockService.addStock(productA, warehouseB, 5);

            StockItem result = stockRepository
                    .findById(new StockKey(productA, warehouseA))
                    .orElseThrow();

            assertThat(result.getAvailable()).isEqualTo(4);
            assertThat(result.getReserved()).isEqualTo(3);
        }
    }
}