package com.inventory.engine.service;

import com.inventory.engine.model.Warehouse;
import com.inventory.engine.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WarehouseServiceTest {

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @BeforeEach
    void clean() {
        warehouseRepository.deleteAll();
    }

    @Test
    void shouldCreateWarehouse() {
        String name = "Main-" + UUID.randomUUID();

        Warehouse w = warehouseService.create(name);

        assertThat(w.getId()).isNotNull();
        assertThat(w.getName()).isEqualTo(name);
    }
}