package com.inventory.engine.controller;

import com.inventory.engine.dto.OrderLineResponse;
import com.inventory.engine.dto.OrderResponse;
import com.inventory.engine.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldReturnCreatedOnCreate() throws Exception {
        OrderResponse response = new OrderResponse(
                12L,
                List.of(new OrderLineResponse(3L, 2)),
                "NEW"
        );

        when(orderService.createOrder(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lines\":[{\"productId\":3,\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/orders/12"))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.lines[0].productId").value(3))
                .andExpect(jsonPath("$.lines[0].quantity").value(2));
    }

    @Test
    void shouldListOrders() throws Exception {
        List<OrderResponse> responses = List.of(
                new OrderResponse(1L, List.of(new OrderLineResponse(7L, 1)), "NEW"),
                new OrderResponse(2L, List.of(new OrderLineResponse(8L, 3)), "RESERVED")
        );

        when(orderService.listOrders()).thenReturn(responses);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].lines[0].productId").value(7))
                .andExpect(jsonPath("$[0].status").value("NEW"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].lines[0].productId").value(8))
                .andExpect(jsonPath("$[1].status").value("RESERVED"));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        OrderResponse response = new OrderResponse(
                9L,
                List.of(new OrderLineResponse(4L, 1)),
                "CANCELLED"
        );

        when(orderService.cancel(9L, 2L)).thenReturn(response);

        mockMvc.perform(post("/api/orders/9/cancel")
                        .param("warehouseId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
