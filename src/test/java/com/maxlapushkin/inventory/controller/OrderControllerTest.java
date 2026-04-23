package com.maxlapushkin.inventory.controller;

import com.maxlapushkin.inventory.dto.OrderLineResponse;
import com.maxlapushkin.inventory.dto.OrderResponse;
import com.maxlapushkin.inventory.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private static final String CUSTOMER_NAME = "Jane Smith";
    private static final String DELIVERY_ADDRESS = "123 Main Street";
    private static final String DELIVERY_CITY = "Budapest";
    private static final String DELIVERY_POSTAL_CODE = "1051";
    private static final String CUSTOMER_PHONE = "+36123456789";
    private static final String CREATE_ORDER_BODY = """
            {
              "customerName":"Jane Smith",
              "deliveryAddress":"123 Main Street",
              "deliveryCity":"Budapest",
              "deliveryPostalCode":"1051",
              "customerPhone":"+36123456789",
              "lines":[{"productId":3,"quantity":2}]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldReturnCreatedOnCreate() throws Exception {
        OrderResponse response = orderResponse(12L, 3L, 2, "CREATED", null);

        when(orderService.createOrder(any())).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_ORDER_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/orders/12"))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.warehouseId").value(nullValue()))
                .andExpect(jsonPath("$.customerName").value(CUSTOMER_NAME))
                .andExpect(jsonPath("$.deliveryAddress").value(DELIVERY_ADDRESS))
                .andExpect(jsonPath("$.deliveryCity").value(DELIVERY_CITY))
                .andExpect(jsonPath("$.deliveryPostalCode").value(DELIVERY_POSTAL_CODE))
                .andExpect(jsonPath("$.customerPhone").value(CUSTOMER_PHONE))
                .andExpect(jsonPath("$.lines[0].productId").value(3))
                .andExpect(jsonPath("$.lines[0].quantity").value(2));
    }

    @Test
    void shouldListOrders() throws Exception {
        List<OrderResponse> responses = List.of(
                orderResponse(1L, 7L, 1, "CREATED", null),
                orderResponse(2L, 8L, 3, "RESERVED", 5L)
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
        OrderResponse response = orderResponse(8L, 6L, 5, "CREATED", null);

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
        OrderResponse response = orderResponse(7L, 5L, 2, "RESERVED", 3L);

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
        OrderResponse response = orderResponse(10L, 4L, 1, "CREATED", null);

        when(orderService.releaseReservation(10L)).thenReturn(response);

        mockMvc.perform(post("/api/orders/10/release"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.warehouseId").value(nullValue()));
    }

    @Test
    void shouldFulfillOrder() throws Exception {
        OrderResponse response = orderResponse(11L, 4L, 1, "FULFILLED", 3L);

        when(orderService.fulfill(11L)).thenReturn(response);

        mockMvc.perform(post("/api/orders/11/fulfill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.status").value("FULFILLED"))
                .andExpect(jsonPath("$.warehouseId").value(3));

        verify(orderService).fulfill(11L);
    }

    @Test
    void shouldCancelOrder() throws Exception {
        OrderResponse response = orderResponse(9L, 4L, 1, "CANCELLED", null);

        when(orderService.cancel(9L)).thenReturn(response);

        mockMvc.perform(post("/api/orders/9/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.warehouseId").value(nullValue()));
    }

    private OrderResponse orderResponse(Long id, Long productId, int quantity, String status, Long warehouseId) {
        return new OrderResponse(
                id,
                List.of(new OrderLineResponse(productId, quantity)),
                CUSTOMER_NAME,
                DELIVERY_ADDRESS,
                DELIVERY_CITY,
                DELIVERY_POSTAL_CODE,
                CUSTOMER_PHONE,
                status,
                warehouseId
        );
    }
}
