package com.maxlapushkin.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Order line response")
public class OrderLineResponse {
    @Schema(description = "Product id", example = "1")
    private Long productId;
    @Schema(description = "Quantity reserved/ordered", example = "2")
    private Integer quantity;
}
