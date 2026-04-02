package com.inventory.engine.service;

import com.inventory.engine.exception.NotEnoughStockException;
import com.inventory.engine.model.Order;
import com.inventory.engine.model.OrderLine;
import com.inventory.engine.model.StockItem;
import com.inventory.engine.model.StockKey;
import com.inventory.engine.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class StockConcurrencyTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Test
    @DisplayName("Concurrent reservation of last unit: only one succeeds")
    void shouldReserveLastUnitAtomically() throws Exception {
        stockService.addStock(1L, 1L, 1);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger notEnough = new AtomicInteger();

        runConcurrent(2, () -> {
            try {
                stockService.reserveStock(1L, 1L, 1);
                success.incrementAndGet();
            } catch (NotEnoughStockException ex) {
                notEnough.incrementAndGet();
            }
        });

        assertThat(success.get()).isEqualTo(1);
        assertThat(notEnough.get()).isEqualTo(1);

        StockItem result = stockRepository.findById(new StockKey(1L, 1L)).orElseThrow();
        assertThat(result.getAvailable()).isEqualTo(0);
        assertThat(result.getReserved()).isEqualTo(1);
    }

    @Test
    @DisplayName("Competing orders for same SKU: only one order reserves stock")
    void shouldAllowOnlyOneOrderToReserve() throws Exception {
        stockService.addStock(2L, 1L, 5);

        Order orderA = new Order(List.of(new OrderLine(2L, 4)));
        Order orderB = new Order(List.of(new OrderLine(2L, 4)));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger notEnough = new AtomicInteger();
        AtomicInteger index = new AtomicInteger();

        runConcurrent(2, () -> {
            try {
                Order order = index.getAndIncrement() == 0 ? orderA : orderB;
                stockService.reserveOrderAtomically(1L, order);
                success.incrementAndGet();
            } catch (NotEnoughStockException ex) {
                notEnough.incrementAndGet();
            }
        });

        assertThat(success.get()).isEqualTo(1);
        assertThat(notEnough.get()).isEqualTo(1);

        StockItem result = stockRepository.findById(new StockKey(2L, 1L)).orElseThrow();
        assertThat(result.getAvailable()).isEqualTo(1);
        assertThat(result.getReserved()).isEqualTo(4);
    }

    @Test
    @DisplayName("Multi-line rollback: if one line fails, no stock changes remain")
    void shouldRollbackAllLinesOnFailure() {
        stockService.addStock(3L, 1L, 3);
        stockService.addStock(4L, 1L, 1);

        Order order = new Order(List.of(
                new OrderLine(3L, 2),
                new OrderLine(4L, 2)
        ));

        assertThatThrownBy(() -> stockService.reserveOrderAtomically(1L, order))
                .isInstanceOf(NotEnoughStockException.class);

        StockItem product1 = stockRepository.findById(new StockKey(3L, 1L)).orElseThrow();
        StockItem product2 = stockRepository.findById(new StockKey(4L, 1L)).orElseThrow();

        assertThat(product1.getAvailable()).isEqualTo(3);
        assertThat(product1.getReserved()).isEqualTo(0);
        assertThat(product2.getAvailable()).isEqualTo(1);
        assertThat(product2.getReserved()).isEqualTo(0);
    }

    private void runConcurrent(int threads, Runnable action) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                ready.countDown();
                try {
                    start.await(5, TimeUnit.SECONDS);
                    action.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();
    }
}
