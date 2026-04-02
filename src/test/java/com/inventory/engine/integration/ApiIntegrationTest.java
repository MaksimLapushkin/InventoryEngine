package com.inventory.engine.integration;

import com.inventory.engine.test.PostgresContainerTestBase;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest extends PostgresContainerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRunHappyPathFlow() throws Exception {
        String sku = "SKU-" + UUID.randomUUID();
        String productBody = "{\"sku\":\"" + sku + "\",\"name\":\"Milk\",\"unit\":\"PIECE\"}";
        MvcResult productResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productBody))
                .andExpect(status().isCreated())
                .andReturn();

        Number productIdValue = JsonPath.read(productResult.getResponse().getContentAsString(), "$.id");
        long productId = productIdValue.longValue();

        MvcResult warehouseResult = mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Main\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        Number warehouseIdValue = JsonPath.read(warehouseResult.getResponse().getContentAsString(), "$.id");
        long warehouseId = warehouseIdValue.longValue();

        mockMvc.perform(post("/api/stocks/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"warehouseId\":" + warehouseId + ",\"quantity\":5}"))
                .andExpect(status().isOk());

        String orderBody = "{\"lines\":[{\"productId\":" + productId + ",\"quantity\":2}]}";
        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andExpect(status().isCreated())
                .andReturn();

        Number orderIdValue = JsonPath.read(orderResult.getResponse().getContentAsString(), "$.id");
        long orderId = orderIdValue.longValue();

        mockMvc.perform(post("/api/orders/" + orderId + "/reserve")
                        .param("warehouseId", String.valueOf(warehouseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVED"));

        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservedQuantity").value(2))
                .andExpect(jsonPath("$[0].availableQuantity").value(3));
    }
}
