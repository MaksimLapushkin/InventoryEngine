package com.inventory.engine.controller;

import com.inventory.engine.dto.AddStockRequest;
import com.inventory.engine.dto.ReserveStockRequest;
import com.inventory.engine.dto.StockResponse;
import com.inventory.engine.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/add")
    public ResponseEntity<Void> add(@Valid @RequestBody AddStockRequest request) {
        stockService.addStock(
                request.getProductId(),
                request.getWarehouseId(),
                request.getQuantity()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@Valid @RequestBody ReserveStockRequest request) {
        stockService.reserveStock(
                request.getProductId(),
                request.getWarehouseId(),
                request.getQuantity()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@Valid @RequestBody ReserveStockRequest request) {
        stockService.releaseStock(
                request.getProductId(),
                request.getWarehouseId(),
                request.getQuantity()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<StockResponse> getStocks(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId
    ) {
        return stockService.getStocks(productId, warehouseId);
    }
}