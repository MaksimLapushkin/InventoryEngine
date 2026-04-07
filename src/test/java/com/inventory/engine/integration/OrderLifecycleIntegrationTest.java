package com.inventory.engine.integration;

import com.inventory.engine.dto.CreateOrderLineRequest;
import com.inventory.engine.dto.CreateOrderRequest;
import com.inventory.engine.dto.OrderResponse;
import com.inventory.engine.exception.NotEnoughStockException;
import com.inventory.engine.model.StockItem;
import com.inventory.engine.model.StockKey;
import com.inventory.engine.model.Unit;
import com.inventory.engine.repository.StockRepository;
import com.inventory.engine.service.OrderService;
import com.inventory.engine.service.ProductService;
import com.inventory.engine.service.StockService;
import com.inventory.engine.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class OrderLifecycleIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long warehouseId;
    private Long productId;

    @BeforeEach
    void setUp() {
        var product = productService.addProduct("SKU-" + UUID.randomUUID(), "Order lifecycle product", Unit.PIECE);
        var warehouse = warehouseService.create("WH-" + UUID.randomUUID());
        productId = product.getId();
        warehouseId = warehouse.getId();
    }

    @Test
    void shouldCreateGetAndListOrdersWithCreatedStatus() {
        OrderResponse created = orderService.createOrder(orderRequest(productId, 2));

        assertThat(created.getStatus()).isEqualTo("CREATED");
        assertThat(created.getWarehouseId()).isNull();

        OrderResponse found = orderService.findById(created.getId());
        List<OrderResponse> orders = orderService.listOrders();

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getStatus()).isEqualTo("CREATED");
        assertThat(found.getWarehouseId()).isNull();
        assertThat(orders).extracting(OrderResponse::getId).contains(created.getId());
    }

    @Test
    void shouldReserveOrderSuccessfully() {
        stockService.addStock(productId, warehouseId, 5);
        OrderResponse created = orderService.createOrder(orderRequest(productId, 2));

        OrderResponse reserved = orderService.reserve(created.getId(), warehouseId);

        assertThat(reserved.getStatus()).isEqualTo("RESERVED");
        assertThat(reserved.getWarehouseId()).isEqualTo(warehouseId);

        StockItem stockItem = stockRepository.findById(new StockKey(productId, warehouseId)).orElseThrow();
        assertThat(stockItem.getReserved()).isEqualTo(2);
        assertThat(stockItem.getAvailable()).isEqualTo(3);
    }

    @Test
    void shouldNotMarkOrderAsReservedWhenReservationFails() {
        stockService.addStock(productId, warehouseId, 1);
        OrderResponse created = orderService.createOrder(orderRequest(productId, 2));

        assertThatThrownBy(() -> orderService.reserve(created.getId(), warehouseId))
                .isInstanceOf(NotEnoughStockException.class);

        OrderResponse found = orderService.findById(created.getId());
        assertThat(found.getStatus()).isEqualTo("CREATED");
        assertThat(found.getWarehouseId()).isNull();
    }

    @Test
    void shouldReleaseReservationBackToCreated() {
        stockService.addStock(productId, warehouseId, 5);
        OrderResponse created = orderService.createOrder(orderRequest(productId, 2));
        orderService.reserve(created.getId(), warehouseId);

        OrderResponse released = orderService.releaseReservation(created.getId());

        assertThat(released.getStatus()).isEqualTo("CREATED");
        assertThat(released.getWarehouseId()).isNull();

        StockItem stockItem = stockRepository.findById(new StockKey(productId, warehouseId)).orElseThrow();
        assertThat(stockItem.getReserved()).isEqualTo(0);
        assertThat(stockItem.getAvailable()).isEqualTo(5);
    }

    @Test
    void shouldCancelCreatedOrderWithoutStockRelease() {
        OrderResponse created = orderService.createOrder(orderRequest(productId, 2));

        OrderResponse cancelled = orderService.cancel(created.getId());

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
        assertThat(cancelled.getWarehouseId()).isNull();
    }

    @Test
    void shouldCancelReservedOrderAndReleaseReservation() {
        stockService.addStock(productId, warehouseId, 5);
        OrderResponse created = orderService.createOrder(orderRequest(productId, 2));
        orderService.reserve(created.getId(), warehouseId);

        OrderResponse cancelled = orderService.cancel(created.getId());

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
        assertThat(cancelled.getWarehouseId()).isNull();

        StockItem stockItem = stockRepository.findById(new StockKey(productId, warehouseId)).orElseThrow();
        assertThat(stockItem.getReserved()).isEqualTo(0);
        assertThat(stockItem.getAvailable()).isEqualTo(5);
    }

    @Test
    void shouldRejectInvalidTransitions() {
        OrderResponse created = orderService.createOrder(orderRequest(productId, 2));

        assertThatThrownBy(() -> orderService.releaseReservation(created.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESERVED");

        orderService.cancel(created.getId());

        assertThatThrownBy(() -> orderService.reserve(created.getId(), warehouseId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREATED");
    }

    @Test
    void shouldRejectReservedOrderWithoutReservationWarehouse() {
        stockService.addStock(productId, warehouseId, 5);
        OrderResponse created = orderService.createOrder(orderRequest(productId, 2));
        orderService.reserve(created.getId(), warehouseId);

        jdbcTemplate.update("UPDATE orders SET warehouse_id = NULL WHERE id = ?", created.getId());

        assertThatThrownBy(() -> orderService.releaseReservation(created.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reservation warehouse");
    }

    private CreateOrderRequest orderRequest(Long requestedProductId, int qty) {
        CreateOrderLineRequest line = new CreateOrderLineRequest();
        line.setProductId(requestedProductId);
        line.setQuantity(qty);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setLines(List.of(line));
        return request;
    }
}
