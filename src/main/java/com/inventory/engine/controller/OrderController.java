package com.inventory.engine.controller;

import com.inventory.engine.dto.CreateOrderRequest;
import com.inventory.engine.dto.OrderResponse;
import com.inventory.engine.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create (@RequestBody CreateOrderRequest request){
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get (@PathVariable Long id){
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<OrderResponse> reserve (
            @PathVariable Long orderId, @RequestBody Long warehouseId){
        return ResponseEntity.ok(orderService.reserve(orderId,warehouseId));
    }
}
