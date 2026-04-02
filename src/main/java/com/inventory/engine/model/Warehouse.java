package com.inventory.engine.model;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouses")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    protected Warehouse() {
    }

    public Warehouse(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("empty name");
        }
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
