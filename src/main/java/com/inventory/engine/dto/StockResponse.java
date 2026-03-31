package com.inventory.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockResponse {
    private Long productId;
    private Long warehouseId;
    private int totalQuantity;
    private int reservedQuantity;
    private int availableQuantity;
}