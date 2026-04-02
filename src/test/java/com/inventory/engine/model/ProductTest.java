package com.inventory.engine.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void shouldRejectNullSku() {
        assertThatThrownBy(() -> new Product(null, "Milk", Unit.PIECE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty sku");
    }

    @Test
    void shouldRejectBlankSku() {
        assertThatThrownBy(() -> new Product("   ", "Milk", Unit.PIECE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty sku");
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> new Product("SKU-1", null, Unit.PIECE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty name");
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> new Product("SKU-1", "   ", Unit.PIECE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty name");
    }

    @Test
    void shouldRejectNullUnit() {
        assertThatThrownBy(() -> new Product("SKU-1", "Milk", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unit cannot be null");
    }
}
