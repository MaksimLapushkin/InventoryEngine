package com.inventory.engine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWarehouseRequest {
    @NotBlank
    private String name;
}
