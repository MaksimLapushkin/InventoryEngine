package com.maxlapushkin.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Create order line request")
public class CreateOrderLineRequest {

    @Schema(description = "Product id", example = "1")
    @NotNull(message = "productId must not be null")
    private Long productId;

    @Schema(description = "Quantity to order", example = "2")
    @NotNull(message = "quantity must not be null")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;
}
