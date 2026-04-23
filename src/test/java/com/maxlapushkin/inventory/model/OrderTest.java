package com.maxlapushkin.inventory.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void shouldStartNewOrderInCreatedStatus() {
        Order order = order();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getWarehouseId()).isNull();
    }

    @Test
    void shouldStoreDeliverySnapshot() {
        Order order = new Order(
                List.of(new OrderLine(7L, 2)),
                "Jane Smith",
                "123 Main Street",
                "Budapest",
                "1051",
                "+36123456789"
        );

        assertThat(order.getCustomerName()).isEqualTo("Jane Smith");
        assertThat(order.getDeliveryAddress()).isEqualTo("123 Main Street");
        assertThat(order.getDeliveryCity()).isEqualTo("Budapest");
        assertThat(order.getDeliveryPostalCode()).isEqualTo("1051");
        assertThat(order.getCustomerPhone()).isEqualTo("+36123456789");
    }

    @Test
    void shouldReserveCreatedOrder() {
        Order order = order();

        order.reserve(3L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.RESERVED);
        assertThat(order.getWarehouseId()).isEqualTo(3L);
    }

    @Test
    void shouldReleaseReservationBackToCreated() {
        Order order = order();
        order.reserve(3L);

        order.releaseReservation();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getWarehouseId()).isNull();
    }

    @Test
    void shouldFulfillReservedOrder() {
        Order order = order();
        order.reserve(3L);

        order.fulfill();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.FULFILLED);
        assertThat(order.getWarehouseId()).isEqualTo(3L);
    }

    @Test
    void shouldCancelCreatedOrder() {
        Order order = order();

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getWarehouseId()).isNull();
    }

    @Test
    void shouldCancelReservedOrderAndClearWarehouse() {
        Order order = order();
        order.reserve(3L);

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getWarehouseId()).isNull();
    }

    @Test
    void shouldRejectInvalidTransitions() {
        Order created = order();
        assertThatThrownBy(created::releaseReservation)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESERVED");
        assertThatThrownBy(created::fulfill)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESERVED");

        Order reserved = order();
        reserved.reserve(3L);
        assertThatThrownBy(() -> reserved.reserve(3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREATED");

        Order fulfilled = order();
        fulfilled.reserve(3L);
        fulfilled.fulfill();
        assertThatThrownBy(fulfilled::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FULFILLED");

        Order cancelled = order();
        cancelled.cancel();
        assertThatThrownBy(() -> cancelled.reserve(3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREATED");
    }

    private Order order() {
        return new Order(List.of(new OrderLine(7L, 2)));
    }
}
