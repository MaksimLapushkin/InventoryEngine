package com.maxlapushkin.inventory.service;

import com.maxlapushkin.inventory.exception.ProductNotFoundException;
import com.maxlapushkin.inventory.model.Product;
import com.maxlapushkin.inventory.model.Unit;
import com.maxlapushkin.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    public ProductService(ProductRepository repository){
        this.repository=repository;
    }

    @Transactional
    public Product addProduct(String sku, String name, Unit unit) {
        Product product = new Product( sku, name, unit);
        repository.save(product);
        return product;
    }

    public Product getProduct(Long id){
        return repository
                .findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<Product> listProducts(){
        return repository.findAll();
    }

    public List<Product> findProductsByUnit(Unit unit){
        return repository.findByUnit(unit);
    }

    public List<Product> findProductsByName(String name){
        if (name==null || name.isBlank()){
            throw new IllegalArgumentException("empty name");
        }
        return repository.findByNameContainingIgnoreCase(name);

    }

}
