package com.inventory.engine.controller;

import com.inventory.engine.dto.CreateWarehouseRequest;
import com.inventory.engine.dto.WarehouseResponse;
import com.inventory.engine.model.Warehouse;
import com.inventory.engine.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody CreateWarehouseRequest request) {
        Warehouse warehouse = warehouseService.create(request.getName());
        return ResponseEntity.ok(map(warehouse));
    }

    @GetMapping
    public List<WarehouseResponse> getAll() {
        return warehouseService.getAll().stream()
                .map(this::map)
                .toList();
    }

    @GetMapping("/{id}")
    public WarehouseResponse getById(@PathVariable Long id) {
        return map(warehouseService.getById(id));
    }

    private WarehouseResponse map(Warehouse w) {
        return new WarehouseResponse(w.getId(), w.getName());
    }
}