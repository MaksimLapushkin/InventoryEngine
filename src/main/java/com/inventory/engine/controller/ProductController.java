package com.inventory.engine.controller;


import com.inventory.engine.dto.CreateProductRequest;
import com.inventory.engine.dto.ProductDto;
import com.inventory.engine.mapper.ProductMapper;
import com.inventory.engine.model.Product;
import com.inventory.engine.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAll(){
        return ResponseEntity.ok(ProductMapper.toDtoList(productService.listProducts()));
    }
    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest request) {

        Product product = productService.addProduct(
                request.getSku(),
                request.getName(),
                request.getUnit()
        );

        return ResponseEntity.ok(ProductMapper.toDto(product));
    }
}
