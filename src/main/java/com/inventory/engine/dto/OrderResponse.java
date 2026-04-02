package com.inventory.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Order response")
public class OrderResponse {
    @Schema(description = "Order id", example = "10")
    private Long id;
    @Schema(description = "Order lines")
    private List<OrderLineResponse> lines;
    @Schema(description = "Order status", example = "NEW")
    private String status;
}
