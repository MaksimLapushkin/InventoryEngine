package com.inventory.engine.service;

import com.inventory.engine.exception.NotEnoughStockException;
import com.inventory.engine.model.Order;
import com.inventory.engine.model.OrderLine;
import com.inventory.engine.model.StockItem;
import com.inventory.engine.model.StockKey;
import com.inventory.engine.model.Unit;
import com.inventory.engine.repository.OrderRepository;
import com.inventory.engine.repository.ProductRepository;
import com.inventory.engine.repository.StockRepository;
import com.inventory.engine.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
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

    @Autowired
    private ProductService productService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @BeforeEach
    void cleanDb() {
        orderRepository.deleteAll();
        stockRepository.deleteAll();
        productRepository.deleteAll();
        warehouseRepository.deleteAll();
    }

    @Test
    @DisplayName("Concurrent reservation of last unit: only one succeeds")
    void shouldReserveLastUnitAtomically() throws Exception {
        Long warehouseId = createWarehouse();
        Long productId = createProduct("Last unit product");

        stockService.addStock(productId, warehouseId, 1);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger notEnough = new AtomicInteger();

        runConcurrent(2, () -> {
            try {
                stockService.reserveStock(productId, warehouseId, 1);
                success.incrementAndGet();
            } catch (NotEnoughStockException ex) {
                notEnough.incrementAndGet();
            }
        });

        assertThat(success.get()).isEqualTo(1);
        assertThat(notEnough.get()).isEqualTo(1);

        StockItem result = stockRepository.findById(new StockKey(productId, warehouseId)).orElseThrow();
        assertThat(result.getAvailable()).isEqualTo(0);
        assertThat(result.getReserved()).isEqualTo(1);
    }

    @Test
    @DisplayName("Competing orders for same SKU: only one order reserves stock")
    void shouldAllowOnlyOneOrderToReserve() throws Exception {
        Long warehouseId = createWarehouse();
        Long productId = createProduct("Competing order product");

        stockService.addStock(productId, warehouseId, 5);

        Order orderA = new Order(List.of(new OrderLine(productId, 4)));
        Order orderB = new Order(List.of(new OrderLine(productId, 4)));

        AtomicInteger success = new AtomicInteger();
        AtomicInteger notEnough = new AtomicInteger();
        AtomicInteger index = new AtomicInteger();

        runConcurrent(2, () -> {
            try {
                Order order = index.getAndIncrement() == 0 ? orderA : orderB;
                stockService.reserveOrderAtomically(warehouseId, order);
                success.incrementAndGet();
            } catch (NotEnoughStockException ex) {
                notEnough.incrementAndGet();
            }
        });

        assertThat(success.get()).isEqualTo(1);
        assertThat(notEnough.get()).isEqualTo(1);

        StockItem result = stockRepository.findById(new StockKey(productId, warehouseId)).orElseThrow();
        assertThat(result.getAvailable()).isEqualTo(1);
        assertThat(result.getReserved()).isEqualTo(4);
    }

    @Test
    @DisplayName("Multi-line rollback: if one line fails, no stock changes remain")
    void shouldRollbackAllLinesOnFailure() {
        Long warehouseId = createWarehouse();
        Long productId1 = createProduct("Rollback product 1");
        Long productId2 = createProduct("Rollback product 2");

        stockService.addStock(productId1, warehouseId, 3);
        stockService.addStock(productId2, warehouseId, 1);

        Order order = new Order(List.of(
                new OrderLine(productId1, 2),
                new OrderLine(productId2, 2)
        ));

        assertThatThrownBy(() -> stockService.reserveOrderAtomically(warehouseId, order))
                .isInstanceOf(NotEnoughStockException.class);

        StockItem product1 = stockRepository.findById(new StockKey(productId1, warehouseId)).orElseThrow();
        StockItem product2 = stockRepository.findById(new StockKey(productId2, warehouseId)).orElseThrow();

        assertThat(product1.getAvailable()).isEqualTo(3);
        assertThat(product1.getReserved()).isEqualTo(0);
        assertThat(product2.getAvailable()).isEqualTo(1);
        assertThat(product2.getReserved()).isEqualTo(0);
    }

    private Long createProduct(String name) {
        String sku = "SKU-" + UUID.randomUUID();
        return productService.addProduct(sku, name, Unit.PIECE).getId();
    }

    private Long createWarehouse() {
        return warehouseService.create("WH-" + UUID.randomUUID()).getId();
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
                    boolean started = start.await(5, TimeUnit.SECONDS);
                    if (started) {
                        action.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        boolean allReady = ready.await(5, TimeUnit.SECONDS);
        assertThat(allReady).isTrue();

        start.countDown();

        boolean allDone = done.await(10, TimeUnit.SECONDS);
        assertThat(allDone).isTrue();

        executor.shutdownNow();
    }
}