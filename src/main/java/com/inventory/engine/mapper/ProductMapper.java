package com.inventory.engine.mapper;

import com.inventory.engine.dto.ProductDto;
import com.inventory.engine.model.Product;

import java.util.List;

public class ProductMapper {

    public static ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getUnit()
        );
    }

    public static List<ProductDto> toDtoList(List<Product> products) {
        return products.stream()
                .map(ProductMapper::toDto)
                .toList();
    }
}