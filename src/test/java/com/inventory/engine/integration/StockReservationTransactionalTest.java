package com.inventory.engine.integration;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class StockReservationTransactionalTest extends PostgresContainerTestBase {

    @Autowired
    private StockService stockService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockRepository stockRepository;

    @Test
    void shouldRollbackOrderReservationWhenLineFails() {
        stockService.addStock(101L, 1L, 3);
        stockService.addStock(102L, 1L, 1);

        var orderRequest = new com.inventory.engine.dto.CreateOrderRequest();
        var line1 = new com.inventory.engine.dto.CreateOrderLineRequest();
        line1.setProductId(101L);
        line1.setQuantity(2);
        var line2 = new com.inventory.engine.dto.CreateOrderLineRequest();
        line2.setProductId(102L);
        line2.setQuantity(2);
        orderRequest.setLines(java.util.List.of(line1, line2));

        var order = orderService.createOrder(orderRequest);

        assertThatThrownBy(() -> orderService.reserve(order.getId(), 1L))
                .isInstanceOf(NotEnoughStockException.class);

        StockItem product1 = stockRepository.findById(new StockKey(101L, 1L)).orElseThrow();
        StockItem product2 = stockRepository.findById(new StockKey(102L, 1L)).orElseThrow();

        assertThat(product1.getAvailable()).isEqualTo(3);
        assertThat(product1.getReserved()).isEqualTo(0);
        assertThat(product2.getAvailable()).isEqualTo(1);
        assertThat(product2.getReserved()).isEqualTo(0);
    }
}
