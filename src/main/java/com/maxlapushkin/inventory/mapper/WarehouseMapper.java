package com.maxlapushkin.inventory.mapper;

import com.maxlapushkin.inventory.dto.WarehouseResponse;
import com.maxlapushkin.inventory.model.Warehouse;

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
