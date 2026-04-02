package com.inventory.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Create order request")
public class CreateOrderRequest {
    @Schema(description = "Order lines")
    @NotEmpty(message = "Order must contain at least one line")
    private List<@Valid CreateOrderLineRequest> lines;
}
