package com.inventory.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private List<OrderLineResponse> lines;
    private String status;
}