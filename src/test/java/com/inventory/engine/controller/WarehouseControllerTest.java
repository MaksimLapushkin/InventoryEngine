package com.inventory.engine.controller;

import com.inventory.engine.model.Warehouse;
import com.inventory.engine.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WarehouseControllerTest {

    private MockMvc mockMvc;
    private WarehouseService warehouseService;

    @BeforeEach
    void setUp() {
        warehouseService = mock(WarehouseService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new WarehouseController(warehouseService))
                .build();
    }

    @Test
    void shouldReturnCreatedOnCreate() throws Exception {
        Warehouse warehouse = new Warehouse("Main");
        ReflectionTestUtils.setField(warehouse, "id", 5L);

        when(warehouseService.create("Main")).thenReturn(warehouse);

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Main\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/warehouses/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Main"));
    }

    @Test
    void shouldDeleteWarehouse() throws Exception {
        doNothing().when(warehouseService).delete(5L);

        mockMvc.perform(delete("/api/warehouses/5"))
                .andExpect(status().isNoContent());

        verify(warehouseService).delete(5L);
    }
}
