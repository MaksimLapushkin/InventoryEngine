package com.inventory.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Stock response")
public class StockResponse {
    @Schema(description = "Product id", example = "1")
    private Long productId;
    @Schema(description = "Warehouse id", example = "1")
    private Long warehouseId;
    @Schema(description = "Total quantity", example = "10")
    private int totalQuantity;
    @Schema(description = "Reserved quantity", example = "3")
    private int reservedQuantity;
    @Schema(description = "Available quantity", example = "7")
    private int availableQuantity;
}
