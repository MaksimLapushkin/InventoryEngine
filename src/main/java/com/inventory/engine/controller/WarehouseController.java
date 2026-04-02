package com.inventory.engine.controller;

import com.inventory.engine.dto.CreateWarehouseRequest;
import com.inventory.engine.dto.WarehouseResponse;
import com.inventory.engine.mapper.WarehouseMapper;
import com.inventory.engine.model.Warehouse;
import com.inventory.engine.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody CreateWarehouseRequest request) {
        Warehouse warehouse = warehouseService.create(request.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(warehouse.getId())
                .toUri();
        return ResponseEntity.created(location).body(WarehouseMapper.toResponse(warehouse));
    }

    @GetMapping
    public List<WarehouseResponse> getAll() {
        return WarehouseMapper.toResponseList(warehouseService.getAll());
    }

    @GetMapping("/{id}")
    public WarehouseResponse getById(@PathVariable Long id) {
        return WarehouseMapper.toResponse(warehouseService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
