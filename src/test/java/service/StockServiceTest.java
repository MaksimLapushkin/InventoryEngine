package service;

import model.StockItem;
import model.StockKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import repository.InMemoryStockRepository;
import repository.StockRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StockService tests")
public class StockServiceTest {

    private StockRepository stockRepository;
    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockRepository = new InMemoryStockRepository();
        stockService = new StockService(stockRepository);
    }

    @Nested
    @DisplayName("addStock()")
    class AddStockTests {

        @Test
        void shouldIncreaseAvailableStock() {
            stockService.addStock(0, 0, 1);

            StockItem result = stockRepository.findByKey(new StockKey(0, 0)).orElseThrow();

            assertThat(result.getProductId()).isEqualTo(0);
            assertThat(result.getWarehouseId()).isEqualTo(0);
            assertThat(result.getAvailable()).isEqualTo(1);
            assertThat(result.getReserved()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("reserveStock()")
    class ReserveStockTests {

        @Test
        void shouldReserveStockSuccessfully() {
            stockService.addStock(0, 0, 1);

            stockService.reserveStock(0, 0, 1);

            StockItem result = stockRepository.findByKey(new StockKey(0, 0)).orElseThrow();

            assertThat(result.getAvailable()).isEqualTo(0);
            assertThat(result.getReserved()).isEqualTo(1);
        }

        @Test
        void shouldThrowExceptionWhenStockNotFound(){
            assertThatThrownBy(() -> stockService.reserveStock(999,999,999))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("stock not found");
        }
        @Test
        void shouldThrowExceptionWhenNotEnoughStock() {
            stockService.addStock(0, 0, 1);

            assertThatThrownBy(() -> stockService.reserveStock(0, 0, 2))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not enough items available");
        }
    }

    @Nested
    @DisplayName("releaseStock()")
    class ReleaseStockTests {

        @Test
        void shouldReleaseStockSuccessfully(){
            stockService.addStock(0,0,5);
            stockService.reserveStock(0,0,3);
            stockService.releaseStock(0,0,2);
            StockItem result = stockRepository.findByKey(new StockKey(0, 0)).orElseThrow();

            assertThat(result.getAvailable()).isEqualTo(4);
            assertThat(result.getReserved()).isEqualTo(1);

        }

        @Test
        void shouldThrowExceptionWhenReleasingMoreThanReserved(){
            stockService.addStock(0,0,5);
            stockService.reserveStock(0,0,3);
            assertThatThrownBy(()-> stockService.releaseStock(0,0,4))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot release more items then reserved");

        }
    }

    @Nested
    @DisplayName("Stock Separation")
    class GetAvailableStock {

        @Test
        void shouldKeepStockSeparatedByProductAndWarehouse(){
            stockService.addStock(0,0,5);
            stockService.addStock(0,0,2);
            stockService.reserveStock(0,0,3);
            stockService.addStock(1,0,5);
            stockService.addStock(0,1,5);
            StockItem result = stockRepository.findByKey(new StockKey(0, 0)).orElseThrow();
            assertThat(result.getAvailable()).isEqualTo(4);
            assertThat(result.getReserved()).isEqualTo(3);
        }
    }

}
