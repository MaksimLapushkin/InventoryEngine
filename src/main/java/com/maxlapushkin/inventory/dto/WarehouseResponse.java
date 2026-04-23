package com.maxlapushkin.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Warehouse response")
public class WarehouseResponse {
    @Schema(description = "Warehouse id", example = "1")
    private Long id;
    @Schema(description = "Warehouse name", example = "Main")
    private String name;
}
