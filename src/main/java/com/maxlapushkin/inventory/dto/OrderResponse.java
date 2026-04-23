package com.maxlapushkin.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Order response")
public class OrderResponse {
    @Schema(description = "Order id", example = "10")
    private Long id;
    @Schema(description = "Order lines")
    private List<OrderLineResponse> lines;
    @Schema(description = "Customer name", example = "Jane Smith")
    private String customerName;
    @Schema(description = "Delivery street address", example = "123 Main Street")
    private String deliveryAddress;
    @Schema(description = "Delivery city", example = "Budapest")
    private String deliveryCity;
    @Schema(description = "Delivery postal code", example = "1051")
    private String deliveryPostalCode;
    @Schema(description = "Customer phone number", example = "+36123456789")
    private String customerPhone;
    @Schema(description = "Order status", example = "CREATED")
    private String status;
    @Schema(description = "Warehouse of the active reservation", example = "3", nullable = true)
    private Long warehouseId;
}
