package com.inventory.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Create warehouse request")
public class CreateWarehouseRequest {
    @Schema(description = "Warehouse name", example = "Main")
    @NotBlank
    private String name;
}
