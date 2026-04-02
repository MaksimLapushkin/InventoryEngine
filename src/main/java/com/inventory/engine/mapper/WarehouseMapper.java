package com.inventory.engine.mapper;

import com.inventory.engine.dto.WarehouseResponse;
import com.inventory.engine.model.Warehouse;

import java.util.List;

public class WarehouseMapper {

    public static WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(warehouse.getId(), warehouse.getName());
    }

    public static List<WarehouseResponse> toResponseList(List<Warehouse> warehouses) {
        return warehouses.stream()
                .map(WarehouseMapper::toResponse)
                .toList();
    }
}
