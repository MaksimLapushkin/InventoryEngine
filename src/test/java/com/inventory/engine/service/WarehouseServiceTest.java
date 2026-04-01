package com.inventory.engine.service;

import com.inventory.engine.model.Warehouse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WarehouseServiceTest {

    @Autowired
    private WarehouseService warehouseService;

    @Test
    void shouldCreateWarehouse() {
        Warehouse w = warehouseService.create("Main");

        assertThat(w.getId()).isNotNull();
        assertThat(w.getName()).isEqualTo("Main");
    }
}