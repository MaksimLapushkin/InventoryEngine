package com.inventory.engine.dto;

import com.inventory.engine.model.Unit;

public class ProductDto {

    private final Long id;
    private final String sku;
    private final String name;
    private final Unit unit;

    public ProductDto(Long id, String sku, String name, Unit unit) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.unit = unit;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public Unit getUnit() {
        return unit;
    }
}