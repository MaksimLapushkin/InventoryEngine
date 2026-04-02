package com.inventory.engine.controller;

import com.inventory.engine.dto.CreateOrderRequest;
import com.inventory.engine.dto.OrderResponse;
import com.inventory.engine.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create (@Valid @RequestBody CreateOrderRequest request){
        OrderResponse response = orderService.createOrder(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.listOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get (@PathVariable Long id){
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping("/{orderId}/reserve")
    public ResponseEntity<OrderResponse> reserve (
            @PathVariable Long orderId, @RequestParam Long warehouseId){
        return ResponseEntity.ok(orderService.reserve(orderId,warehouseId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel (
            @PathVariable Long orderId, @RequestParam Long warehouseId){
        return ResponseEntity.ok(orderService.cancel(orderId, warehouseId));
    }
}
