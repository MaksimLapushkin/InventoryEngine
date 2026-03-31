package com.inventory.engine.model;

public class OrderLine {
    private final Long productId;
    private final int quantity;

    public OrderLine(Long productId, int quantity){
        if (quantity<=0){
            throw new IllegalArgumentException("quantity must be positive");}
        this.quantity = quantity;
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

}
