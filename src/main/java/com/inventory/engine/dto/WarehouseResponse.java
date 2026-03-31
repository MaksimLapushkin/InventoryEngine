package com.inventory.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WarehouseResponse {
    private Long id;
    private String name;
}
