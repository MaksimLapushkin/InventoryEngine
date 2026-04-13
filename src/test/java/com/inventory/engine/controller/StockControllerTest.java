package com.inventory.engine.controller;

import com.inventory.engine.exception.GlobalExceptionHandler;
import com.inventory.engine.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
@Import(GlobalExceptionHandler.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @Test
    void shouldAddStock() throws Exception {
        mockMvc.perform(post("/api/stocks/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"warehouseId\":2,\"quantity\":3}"))
                .andExpect(status().isOk());

        verify(stockService).addStock(1L, 2L, 3);
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/stocks/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":null,\"warehouseId\":2,\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(stockService);
    }
}
