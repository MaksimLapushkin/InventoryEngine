package com.inventory.engine.controller;

import com.inventory.engine.dto.AddStockRequest;
import com.inventory.engine.dto.StockResponse;
import com.inventory.engine.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Stock operations")
public class StockController {

    private final StockService stockService;

    @PostMapping("/add")
    @Operation(summary = "Add stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock added"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Stock not found")
    })
    public ResponseEntity<Void> add(@Valid @RequestBody AddStockRequest request) {
        stockService.addStock(
                request.getProductId(),
                request.getWarehouseId(),
                request.getQuantity()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get stock levels")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock levels retrieved")
    })
    public List<StockResponse> getStocks(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId
    ) {
        return stockService.getStocks(productId, warehouseId);
    }
}
