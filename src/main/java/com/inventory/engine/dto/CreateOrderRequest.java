package com.inventory.engine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotEmpty(message = "Order must contain at least one line")
    private List<@Valid CreateOrderLineRequest> lines;
}