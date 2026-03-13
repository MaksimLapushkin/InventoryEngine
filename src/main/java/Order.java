import java.util.ArrayList;
import java.util.List;

public class Order {
    private final int id;
    private OrderStatus status;
    private List<OrderLine> items = new ArrayList<>();

    public Order(int id, List<OrderLine>items) {
        this.id = id;
        this.items = items;
        this.status = OrderStatus.NEW;
    }

    public int getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderLine> getItems() {
        return items;
    }
    public void reserve(){
        if (getStatus() != OrderStatus.NEW) {
            throw new IllegalArgumentException("wrong status");
        }
        this.status = OrderStatus.RESERVED;
    }
    public void confirm(){
        if (getStatus() != OrderStatus.RESERVED) {
            throw new IllegalArgumentException("wrong status");
        }
        this.status = OrderStatus.CONFIRMED;
    }
    public void cancel(){
        if (getStatus() != OrderStatus.CONFIRMED || getStatus() != OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("wrong status");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
