package com.inventory.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderLineResponse {
    private Long productId;
    private Integer quantity;
}