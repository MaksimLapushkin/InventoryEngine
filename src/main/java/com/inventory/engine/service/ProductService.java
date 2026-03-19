package com.inventory.engine.service;

import com.inventory.engine.model.Product;
import com.inventory.engine.model.Unit;
import com.inventory.engine.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ProductService {
    private final ProductRepository repository;
    private int productId = 0;
    public ProductService(ProductRepository repository){
        this.repository=repository;
    }

    public Product addProduct(String sku, String name, Unit unit) {
        Product product = new Product(productId++, sku, name, unit);
        repository.save(product);
        return product;
    }
    public Product getProduct(int id){
        return repository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such product found"));
    }
    public List<Product> listProducts(){
        return repository.findAll();
    }
    public List<Product> findProductsByUnit(Unit unit){
        return  repository.findAll().stream()
                .filter(product -> product.getUnit()==unit)
                .collect(Collectors.toList());
    }
    public List<Product> findProductsByName(String name){
        if (name==null || name.isBlank()){
            throw new IllegalArgumentException("empty name");
        }
        return repository.findAll().stream()
                .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();

    }

}
