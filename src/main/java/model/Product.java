package model;

public class Product {
    private final int id;
    private final String sku;
    private final String name;
    private final Unit unit;

    public Product(int id,String sku,String name,Unit unit){
        this.id = id;
        this.name =name;
        this.sku = sku;
        this.unit = unit;
    }

    public int getId(){
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
        return "model.Product{id=" + id +
                ", sku=" + sku +
                ", name='" + name +
                "', unit=" + unit +
                '}';
    }

}
