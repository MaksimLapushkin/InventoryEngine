package com.inventory.engine.model;

public class Product {
    private final Long id;
    private final String sku;
    private final String name;
    private final Unit unit;

    public Product(Long id,String sku,String name,Unit unit){
        this.id = id;
        this.name =name;
        this.sku = sku;
        this.unit = unit;
    }

    public Long getId(){
        return id;
    }
    public String getSku(){
        return sku;
    }
    public String getName(){
        return name;
    }
    public  Unit getUnit(){
        return  unit;
    }

    @Override
    public String toString() {
        return "com.inventory.engine.model.Product{id=" + id +
                ", sku=" + sku +
                ", name='" + name +
                "', unit=" + unit +
                '}';
    }

}
