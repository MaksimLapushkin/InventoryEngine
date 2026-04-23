package com.maxlapushkin.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Create order request")
public class CreateOrderRequest {
    @Schema(description = "Customer name", example = "Jane Smith")
    @NotBlank(message = "Customer name must not be blank")
    private String customerName;

    @Schema(description = "Delivery street address", example = "123 Main Street")
    @NotBlank(message = "Delivery address must not be blank")
    private String deliveryAddress;

    @Schema(description = "Delivery city", example = "Budapest")
    @NotBlank(message = "Delivery city must not be blank")
    private String deliveryCity;

    @Schema(description = "Delivery postal code", example = "1051")
    @NotBlank(message = "Delivery postal code must not be blank")
    private String deliveryPostalCode;

    @Schema(description = "Customer phone number", example = "+36123456789")
    @NotBlank(message = "Customer phone must not be blank")
    private String customerPhone;

    @Schema(description = "Order lines")
    @NotEmpty(message = "Order must contain at least one line")
    private List<@Valid CreateOrderLineRequest> lines;
}
