package com.maxlapushkin.inventory.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarehouseTest {

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> new Warehouse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty name");
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> new Warehouse("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty name");
    }
}
