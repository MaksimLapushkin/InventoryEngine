package com.inventory.engine.dto;

import lombok.Data;

@Data
public class CreateOrderLineRequest {
    private Long productId;
    private Integer quantity;
}