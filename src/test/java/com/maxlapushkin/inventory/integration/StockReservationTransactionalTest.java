package com.maxlapushkin.inventory.integration;

import com.maxlapushkin.inventory.dto.CreateOrderLineRequest;
import com.maxlapushkin.inventory.dto.CreateOrderRequest;
import com.maxlapushkin.inventory.exception.NotEnoughStockException;
import com.maxlapushkin.inventory.model.StockItem;
import com.maxlapushkin.inventory.model.StockKey;
import com.maxlapushkin.inventory.model.Unit;
import com.maxlapushkin.inventory.repository.StockRepository;
import com.maxlapushkin.inventory.service.OrderService;
import com.maxlapushkin.inventory.service.ProductService;
import com.maxlapushkin.inventory.service.StockService;
import com.maxlapushkin.inventory.service.WarehouseService;
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

        var orderRequest = new CreateOrderRequest();
        var line1 = new CreateOrderLineRequest();
        line1.setProductId(productId1);
        line1.setQuantity(2);
        var line2 = new CreateOrderLineRequest();
        line2.setProductId(productId2);
        line2.setQuantity(2);
        orderRequest.setCustomerName("Jane Smith");
        orderRequest.setDeliveryAddress("123 Main Street");
        orderRequest.setDeliveryCity("Budapest");
        orderRequest.setDeliveryPostalCode("1051");
        orderRequest.setCustomerPhone("+36123456789");
        orderRequest.setLines(java.util.List.of(line1, line2));

        var order = orderService.createOrder(orderRequest);

        assertThatThrownBy(() -> orderService.reserve(order.getId(), warehouseId))
                .isInstanceOf(NotEnoughStockException.class);

        assertThat(orderService.findById(order.getId()).getStatus()).isEqualTo("CREATED");

        StockItem product1Stock = stockRepository.findById(new StockKey(productId1, warehouseId)).orElseThrow();
        StockItem product2Stock = stockRepository.findById(new StockKey(productId2, warehouseId)).orElseThrow();

        assertThat(product1Stock.getAvailable()).isEqualTo(3);
        assertThat(product1Stock.getReserved()).isEqualTo(0);
        assertThat(product2Stock.getAvailable()).isEqualTo(1);
        assertThat(product2Stock.getReserved()).isEqualTo(0);
    }
}
