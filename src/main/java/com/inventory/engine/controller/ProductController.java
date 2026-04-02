package com.inventory.engine.controller;


import com.inventory.engine.dto.CreateProductRequest;
import com.inventory.engine.dto.ProductResponse;
import com.inventory.engine.mapper.ProductMapper;
import com.inventory.engine.model.Product;
import com.inventory.engine.service.ProductService;
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
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll(){
        return ResponseEntity.ok(ProductMapper.toDtoList(productService.listProducts()));
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return ProductMapper.toDto(productService.getProduct(id));
    }

    @PostMapping
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
