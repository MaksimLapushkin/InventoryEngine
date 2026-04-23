package com.maxlapushkin.inventory.controller;

import com.maxlapushkin.inventory.dto.CreateOrderRequest;
import com.maxlapushkin.inventory.dto.OrderResponse;
import com.maxlapushkin.inventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Orders", description = "Order operations")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<OrderResponse> create (@Valid @RequestBody CreateOrderRequest request){
        OrderResponse response = orderService.createOrder(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orderId}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "List orders")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved")
    })
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.listOrders());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order retrieved"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> get (@PathVariable Long orderId){
        return ResponseEntity.ok(orderService.findById(orderId));
    }

    @PostMapping("/{orderId}/reserve")
    @Operation(summary = "Reserve order stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order reserved"),
            @ApiResponse(responseCode = "400", description = "Not enough stock or validation failed"),
            @ApiResponse(responseCode = "404", description = "Order or stock not found")
    })
    public ResponseEntity<OrderResponse> reserve (
            @PathVariable Long orderId, @RequestParam Long warehouseId){
        return ResponseEntity.ok(orderService.reserve(orderId,warehouseId));
    }

    @PostMapping("/{orderId}/fulfill")
    @Operation(summary = "Fulfill order stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order fulfilled"),
            @ApiResponse(responseCode = "400", description = "Cannot fulfill reservation"),
            @ApiResponse(responseCode = "404", description = "Order or stock not found")
    })
    public ResponseEntity<OrderResponse> fulfill (
            @PathVariable Long orderId){
        return ResponseEntity.ok(orderService.fulfill(orderId));
    }

    @PostMapping("/{orderId}/release")
    @Operation(summary = "Release order reservation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order reservation released"),
            @ApiResponse(responseCode = "400", description = "Cannot release reservation"),
            @ApiResponse(responseCode = "404", description = "Order or stock not found")
    })
    public ResponseEntity<OrderResponse> releaseReservation(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.releaseReservation(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled"),
            @ApiResponse(responseCode = "400", description = "Cannot cancel order"),
            @ApiResponse(responseCode = "404", description = "Order or stock not found")
    })
    public ResponseEntity<OrderResponse> cancel (@PathVariable Long orderId){
        return ResponseEntity.ok(orderService.cancel(orderId));
    }
}
