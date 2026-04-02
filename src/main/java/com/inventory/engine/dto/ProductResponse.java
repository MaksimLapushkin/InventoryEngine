package com.inventory.engine.dto;

import com.inventory.engine.model.Unit;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product response")
public class ProductResponse {

    @Schema(description = "Product id", example = "1")
    private final Long id;
    @Schema(description = "Unique product SKU", example = "SKU-123")
    private final String sku;
    @Schema(description = "Product name", example = "Milk")
    private final String name;
    @Schema(description = "Unit of measure", example = "PIECE")
    private final Unit unit;

    public ProductResponse(Long id, String sku, String name, Unit unit) {
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
