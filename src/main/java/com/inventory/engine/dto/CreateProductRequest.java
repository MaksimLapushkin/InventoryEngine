package com.inventory.engine.dto;
import com.inventory.engine.model.Unit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateProductRequest {
    @NotBlank(message = "SKU must not be blank")
    private String sku;
    @NotBlank(message = "Name must not be blank")
    private String name;
    @NotNull(message = "Unit must not be null")
    private Unit unit;
}