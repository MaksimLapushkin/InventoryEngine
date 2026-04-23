package com.maxlapushkin.inventory.mapper;

import com.maxlapushkin.inventory.dto.ProductResponse;
import com.maxlapushkin.inventory.model.Product;

import java.util.List;

public class ProductMapper {

    public static ProductResponse toDto(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getUnit()
        );
    }

    public static List<ProductResponse> toDtoList(List<Product> products) {
        return products.stream()
                .map(ProductMapper::toDto)
                .toList();
    }
}