package com.maxlapushkin.inventory.dto;
import com.maxlapushkin.inventory.model.Unit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Create product request")
public class CreateProductRequest {
    @Schema(description = "Unique product SKU", example = "SKU-123")
    @NotBlank(message = "SKU must not be blank")
    private String sku;
    @Schema(description = "Product name", example = "Milk")
    @NotBlank(message = "Name must not be blank")
    private String name;
    @Schema(description = "Unit of measure", example = "PIECE")
    @NotNull(message = "Unit must not be null")
    private Unit unit;
}
