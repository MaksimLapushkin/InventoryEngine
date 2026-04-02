package com.inventory.engine.controller;


import com.inventory.engine.dto.CreateProductRequest;
import com.inventory.engine.dto.ProductResponse;
import com.inventory.engine.mapper.ProductMapper;
import com.inventory.engine.model.Product;
import com.inventory.engine.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog operations")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved")
    })
    public ResponseEntity<List<ProductResponse>> getAll(){
        return ResponseEntity.ok(ProductMapper.toDtoList(productService.listProducts()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product retrieved"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductResponse getById(@PathVariable Long id) {
        return ProductMapper.toDto(productService.getProduct(id));
    }

    @PostMapping
    @Operation(summary = "Create product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {

        Product product = productService.addProduct(
                request.getSku(),
                request.getName(),
                request.getUnit()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.getId())
                .toUri();
        return ResponseEntity.created(location).body(ProductMapper.toDto(product));
    }
}
