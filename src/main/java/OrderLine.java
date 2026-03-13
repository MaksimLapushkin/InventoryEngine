public class OrderLine {
    private final int productId;
    private int quantity;

    public OrderLine(int productId, int quantity){
        if (quantity<=0){
            throw new IllegalArgumentException("quantity must be positive");}
        this.quantity = quantity;
        this.productId = productId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

}
