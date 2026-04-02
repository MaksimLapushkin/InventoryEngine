package com.inventory.engine.controller;

import com.inventory.engine.model.Product;
import com.inventory.engine.model.Unit;
import com.inventory.engine.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest {

    private MockMvc mockMvc;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(productService))
                .build();
    }

    @Test
    void shouldReturnCreatedOnCreate() throws Exception {
        Product product = new Product("SKU-1", "Milk", Unit.PIECE);
        ReflectionTestUtils.setField(product, "id", 10L);

        when(productService.addProduct("SKU-1", "Milk", Unit.PIECE)).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-1\",\"name\":\"Milk\",\"unit\":\"PIECE\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/products/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.sku").value("SKU-1"))
                .andExpect(jsonPath("$.name").value("Milk"))
                .andExpect(jsonPath("$.unit").value("PIECE"));
    }
}
