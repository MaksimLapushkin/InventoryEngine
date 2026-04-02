package com.inventory.engine.service;

import com.inventory.engine.exception.NotEnoughStockException;
import com.inventory.engine.exception.StockNotFoundException;
import com.inventory.engine.model.StockItem;
import com.inventory.engine.model.StockKey;
import com.inventory.engine.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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

    @Nested
    @DisplayName("addStock()")
    class AddStockTests {

        @Test
        void shouldIncreaseAvailableStock() {
            stockService.addStock(0L, 0L, 1);

            StockItem result = stockRepository
                    .findById(new StockKey(0L, 0L))
                    .orElseThrow();

            assertThat(result.getProductId()).isEqualTo(0L);
            assertThat(result.getWarehouseId()).isEqualTo(0L);
            assertThat(result.getAvailable()).isEqualTo(1);
            assertThat(result.getReserved()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("reserveStock()")
    class ReserveStockTests {

        @Test
        void shouldReserveStockSuccessfully() {
            stockService.addStock(0L, 0L, 1);

            stockService.reserveStock(0L, 0L, 1);

            StockItem result = stockRepository
                    .findById(new StockKey(0L, 0L))
                    .orElseThrow();

            assertThat(result.getAvailable()).isEqualTo(0);
            assertThat(result.getReserved()).isEqualTo(1);
        }

        @Test
        void shouldThrowExceptionWhenStockNotFound() {
            assertThatThrownBy(() -> stockService.reserveStock(999L, 999L, 999))
                    .isInstanceOf(StockNotFoundException.class);
        }

        @Test
        void shouldThrowExceptionWhenNotEnoughStock() {
            stockService.addStock(0L, 0L, 1);

            assertThatThrownBy(() -> stockService.reserveStock(0L, 0L, 2))
                    .isInstanceOf(NotEnoughStockException.class);
        }
    }

    @Nested
    @DisplayName("releaseStock()")
    class ReleaseStockTests {

        @Test
        void shouldReleaseStockSuccessfully() {
            stockService.addStock(0L, 0L, 5);
            stockService.reserveStock(0L, 0L, 3);
            stockService.releaseStock(0L, 0L, 2);

            StockItem result = stockRepository
                    .findById(new StockKey(0L, 0L))
                    .orElseThrow();

            assertThat(result.getAvailable()).isEqualTo(4);
            assertThat(result.getReserved()).isEqualTo(1);
        }

        @Test
        void shouldThrowExceptionWhenReleasingMoreThanReserved() {
            stockService.addStock(0L, 0L, 5);
            stockService.reserveStock(0L, 0L, 3);

            assertThatThrownBy(() -> stockService.releaseStock(0L, 0L, 4))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot release more items than reserved");
        }
    }

    @Nested
    @DisplayName("Stock Separation")
    class GetAvailableStock {

        @Test
        void shouldKeepStockSeparatedByProductAndWarehouse() {
            stockService.addStock(0L, 0L, 5);
            stockService.addStock(0L, 0L, 2);
            stockService.reserveStock(0L, 0L, 3);

            stockService.addStock(1L, 0L, 5);
            stockService.addStock(0L, 1L, 5);

            StockItem result = stockRepository
                    .findById(new StockKey(0L, 0L))
                    .orElseThrow();

            assertThat(result.getAvailable()).isEqualTo(4);
            assertThat(result.getReserved()).isEqualTo(3);
        }
    }
}
