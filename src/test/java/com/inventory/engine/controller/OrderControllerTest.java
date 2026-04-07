package com.inventory.engine.controller;

import com.inventory.engine.dto.OrderLineResponse;
import com.inventory.engine.dto.OrderResponse;
import com.inventory.engine.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
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
                "CREATED",
                null
        );

        when(orderService.createOrder(any())).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lines\":[{\"productId\":3,\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/orders/12"))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.warehouseId").value(nullValue()))
                .andExpect(jsonPath("$.lines[0].productId").value(3))
                .andExpect(jsonPath("$.lines[0].quantity").value(2));
    }

    @Test
    void shouldListOrders() throws Exception {
        List<OrderResponse> responses = List.of(
                new OrderResponse(1L, List.of(new OrderLineResponse(7L, 1)), "CREATED", null),
                new OrderResponse(2L, List.of(new OrderLineResponse(8L, 3)), "RESERVED", 5L)
        );

        when(orderService.listOrders()).thenReturn(responses);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].lines[0].productId").value(7))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].warehouseId").value(nullValue()))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].lines[0].productId").value(8))
                .andExpect(jsonPath("$[1].status").value("RESERVED"))
                .andExpect(jsonPath("$[1].warehouseId").value(5));
    }

    @Test
    void shouldGetOrder() throws Exception {
        OrderResponse response = new OrderResponse(
                8L,
                List.of(new OrderLineResponse(6L, 5)),
                "CREATED",
                null
        );

        when(orderService.findById(8L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.warehouseId").value(nullValue()))
                .andExpect(jsonPath("$.lines[0].productId").value(6))
                .andExpect(jsonPath("$.lines[0].quantity").value(5));
    }

    @Test
    void shouldReserveOrder() throws Exception {
        OrderResponse response = new OrderResponse(
                7L,
                List.of(new OrderLineResponse(5L, 2)),
                "RESERVED",
                3L
        );

        when(orderService.reserve(7L, 3L)).thenReturn(response);

        mockMvc.perform(post("/api/orders/7/reserve")
                        .param("warehouseId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.warehouseId").value(3));
    }

    @Test
    void shouldReleaseOrderReservation() throws Exception {
        OrderResponse response = new OrderResponse(
                10L,
                List.of(new OrderLineResponse(4L, 1)),
                "CREATED",
                null
        );

        when(orderService.releaseReservation(10L)).thenReturn(response);

        mockMvc.perform(post("/api/orders/10/release"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.warehouseId").value(nullValue()));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        OrderResponse response = new OrderResponse(
                9L,
                List.of(new OrderLineResponse(4L, 1)),
                "CANCELLED",
                null
        );

        when(orderService.cancel(9L)).thenReturn(response);

        mockMvc.perform(post("/api/orders/9/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.warehouseId").value(nullValue()));
    }
}
