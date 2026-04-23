package com.maxlapushkin.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    protected Product() {
    }

    public Product(String sku, String name, Unit unit) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("empty sku");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("empty name");
        }
        if (unit == null) {
            throw new IllegalArgumentException("unit cannot be null");
        }
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
