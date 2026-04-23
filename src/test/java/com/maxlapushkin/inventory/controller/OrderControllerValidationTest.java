package com.maxlapushkin.inventory.controller;

import com.maxlapushkin.inventory.exception.GlobalExceptionHandler;
import com.maxlapushkin.inventory.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldRejectEmptyLines() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName":"Jane Smith",
                                  "deliveryAddress":"123 Main Street",
                                  "deliveryCity":"Budapest",
                                  "deliveryPostalCode":"1051",
                                  "customerPhone":"+36123456789",
                                  "lines":[]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.violations[0].field").value("lines"))
                .andExpect(jsonPath("$.violations[0].message").value("Order must contain at least one line"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectInvalidLine() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName":"Jane Smith",
                                  "deliveryAddress":"123 Main Street",
                                  "deliveryCity":"Budapest",
                                  "deliveryPostalCode":"1051",
                                  "customerPhone":"+36123456789",
                                  "lines":[{"productId":null,"quantity":1}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.violations[0].field").value("lines[0].productId"))
                .andExpect(jsonPath("$.violations[0].message").value("productId must not be null"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectBlankCustomerName() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName":" ",
                                  "deliveryAddress":"123 Main Street",
                                  "deliveryCity":"Budapest",
                                  "deliveryPostalCode":"1051",
                                  "customerPhone":"+36123456789",
                                  "lines":[{"productId":3,"quantity":2}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/orders"))
                .andExpect(jsonPath("$.violations[0].field").value("customerName"))
                .andExpect(jsonPath("$.violations[0].message").value("Customer name must not be blank"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(orderService);
    }
}
