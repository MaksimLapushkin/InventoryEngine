package com.inventory.engine.integration;

import com.inventory.engine.dto.CreateOrderLineRequest;
import com.inventory.engine.dto.CreateOrderRequest;
import com.inventory.engine.exception.NotEnoughStockException;
import com.inventory.engine.model.StockItem;
import com.inventory.engine.model.StockKey;
import com.inventory.engine.repository.StockRepository;
import com.inventory.engine.service.OrderService;
import com.inventory.engine.service.StockService;
import com.inventory.engine.test.PostgresContainerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StockReservationConcurrencyTest extends PostgresContainerTestBase {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private OrderService orderService;

    @Test
    void shouldAllowOnlyOneReservationForLastUnit() throws Exception {
        stockService.addStock(201L, 1L, 1);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        runConcurrent(2, () -> {
            try {
                stockService.reserveStock(201L, 1L, 1);
                success.incrementAndGet();
            } catch (NotEnoughStockException ex) {
                failed.incrementAndGet();
            }
        });

        assertThat(success.get()).isEqualTo(1);
        assertThat(failed.get()).isEqualTo(1);

        StockItem result = stockRepository.findById(new StockKey(201L, 1L)).orElseThrow();
        assertThat(result.getAvailable()).isEqualTo(0);
        assertThat(result.getReserved()).isEqualTo(1);
    }

    @Test
    void shouldAllowOnlyOneOrderToReserveStock() throws Exception {
        stockService.addStock(202L, 1L, 5);

        CreateOrderRequest orderRequest1 = orderRequest(202L, 4);
        CreateOrderRequest orderRequest2 = orderRequest(202L, 4);
        var order1 = orderService.createOrder(orderRequest1);
        var order2 = orderService.createOrder(orderRequest2);
        Long orderId1 = order1.getId();
        Long orderId2 = order2.getId();

        assertThat(order1.getStatus()).isEqualTo("CREATED");
        assertThat(order2.getStatus()).isEqualTo("CREATED");

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        AtomicInteger index = new AtomicInteger();
        Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

        runConcurrent(2, () -> {
            Long orderId = index.getAndIncrement() == 0 ? orderId1 : orderId2;
            try {
                orderService.reserve(orderId, 1L);
                success.incrementAndGet();
            } catch (Exception ex) {
                failures.add(ex);
                failed.incrementAndGet();
            }
        });

        assertThat(success.get()).isEqualTo(1);
        assertThat(failed.get())
                .as("Captured concurrent reservation failures: %s", failures)
                .isEqualTo(1);
        assertThat(failures)
                .as("Captured concurrent reservation failures")
                .hasSize(1);

        StockItem result = stockRepository.findById(new StockKey(202L, 1L)).orElseThrow();
        assertThat(result.getReserved()).isEqualTo(4);
        assertThat(result.getAvailable()).isEqualTo(1);
    }

    private CreateOrderRequest orderRequest(Long productId, int qty) {
        CreateOrderLineRequest line = new CreateOrderLineRequest();
        line.setProductId(productId);
        line.setQuantity(qty);
        CreateOrderRequest request = new CreateOrderRequest();
        request.setLines(List.of(line));
        return request;
    }

    private void runConcurrent(int threads, Runnable action) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        try {
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

            assertThat(ready.await(5, TimeUnit.SECONDS))
                    .as("All worker threads should be ready before starting")
                    .isTrue();
            start.countDown();
            assertThat(done.await(10, TimeUnit.SECONDS))
                    .as("All worker threads should finish in time")
                    .isTrue();
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                assertThat(executor.awaitTermination(5, TimeUnit.SECONDS))
                        .as("Executor should terminate after forced shutdown")
                        .isTrue();
            }
        }
    }
}
