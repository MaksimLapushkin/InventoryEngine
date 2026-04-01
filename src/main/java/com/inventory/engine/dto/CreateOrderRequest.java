package com.inventory.engine.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private List<CreateOrderLineRequest> lines;
}