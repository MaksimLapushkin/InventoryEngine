package com.inventory.engine.model;

public class StockItem {
    private final int productId;
    private final int warehouseId;
    private int available;
    private int reserved;

    public StockItem(int productId,int warehouseId,int available,int reserved){
        if (available >=0){
        this.available = available;}
        else{
            throw new IllegalArgumentException("available cannot be negative");}
        this.productId = productId;
        this.warehouseId = warehouseId;
        if (reserved>=0){
        this.reserved = reserved;}
        else{
            throw new IllegalArgumentException("reserved cannot be negative");}
    }

    public int getProductId(){
        return productId;
    }
    public int getWarehouseId(){
        return warehouseId;
    }
    public int getAvailable(){
        return available;
    }
    public int getReserved(){
        return reserved;
    }
    public int getTotal(){
        return reserved+available;
    }
    public void addStock(int qty){
        if (qty <= 0){
            throw new IllegalArgumentException("qty must be positive");
        }
        available+=qty;
    }

    public void reserve(int qty){
        if (qty <= 0){
            throw new IllegalArgumentException("qty must be positive");
        }
        if (qty > available){
            throw new IllegalStateException("not enough items available");
        }
        available-=qty;
        reserved+=qty;
    }

    public void releaseReservation(int qty){
        if (qty <= 0){
            throw new IllegalArgumentException("qty must be positive");
        }
        if (qty > reserved){
            throw new IllegalStateException("cannot release more items then reserved");
        }
        available+=qty;
        reserved-=qty;
    }

}
