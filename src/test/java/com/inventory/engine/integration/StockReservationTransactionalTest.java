package com.inventory.engine.integration;

import com.inventory.engine.exception.NotEnoughStockException;
import com.inventory.engine.model.StockItem;
import com.inventory.engine.model.StockKey;
import com.inventory.engine.model.Unit;
import com.inventory.engine.repository.StockRepository;
import com.inventory.engine.service.OrderService;
import com.inventory.engine.service.ProductService;
import com.inventory.engine.service.StockService;
import com.inventory.engine.service.WarehouseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class StockReservationTransactionalTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private StockRepository stockRepository;

    @Test
    void shouldRollbackOrderReservationWhenLineFails() {

        var product1 = productService.addProduct("SKU-" + UUID.randomUUID(), "Product 1", Unit.PIECE);
        var product2 = productService.addProduct("SKU-" + UUID.randomUUID(), "Product 2", Unit.PIECE);
        var warehouse = warehouseService.create("Main " + UUID.randomUUID());

        Long warehouseId = warehouse.getId();
        Long productId1 = product1.getId();
        Long productId2 = product2.getId();

        stockService.addStock(productId1, warehouseId, 3);
        stockService.addStock(productId2, warehouseId, 1);

        var orderRequest = new com.inventory.engine.dto.CreateOrderRequest();
        var line1 = new com.inventory.engine.dto.CreateOrderLineRequest();
        line1.setProductId(productId1);
        line1.setQuantity(2);
        var line2 = new com.inventory.engine.dto.CreateOrderLineRequest();
        line2.setProductId(productId2);
        line2.setQuantity(2);
        orderRequest.setLines(java.util.List.of(line1, line2));

        var order = orderService.createOrder(orderRequest);

        assertThatThrownBy(() -> orderService.reserve(order.getId(), warehouseId))
                .isInstanceOf(NotEnoughStockException.class);

        StockItem product1Stock = stockRepository.findById(new StockKey(productId1, warehouseId)).orElseThrow();
        StockItem product2Stock = stockRepository.findById(new StockKey(productId2, warehouseId)).orElseThrow();

        assertThat(product1Stock.getAvailable()).isEqualTo(3);
        assertThat(product1Stock.getReserved()).isEqualTo(0);
        assertThat(product2Stock.getAvailable()).isEqualTo(1);
        assertThat(product2Stock.getReserved()).isEqualTo(0);
    }
}
