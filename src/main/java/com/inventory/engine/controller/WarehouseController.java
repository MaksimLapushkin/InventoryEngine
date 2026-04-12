package com.inventory.engine.controller;

import com.inventory.engine.dto.CreateWarehouseRequest;
import com.inventory.engine.dto.WarehouseResponse;
import com.inventory.engine.mapper.WarehouseMapper;
import com.inventory.engine.model.Warehouse;
import com.inventory.engine.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses", description = "Warehouse operations")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @Operation(summary = "Create warehouse")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Warehouse created"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody CreateWarehouseRequest request) {
        Warehouse warehouse = warehouseService.create(request.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(warehouse.getId())
                .toUri();
        return ResponseEntity.created(location).body(WarehouseMapper.toResponse(warehouse));
    }

    @GetMapping
    @Operation(summary = "List warehouses")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouses retrieved")
    })
    public List<WarehouseResponse> getAll() {
        return WarehouseMapper.toResponseList(warehouseService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Warehouse retrieved"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found")
    })
    public WarehouseResponse getById(@PathVariable Long id) {
        return WarehouseMapper.toResponse(warehouseService.getById(id));
    }
}
