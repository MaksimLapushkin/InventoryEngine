import java.util.List;
import java.util.Optional;

public class StockService {

    private final StockRepository repository;

    public StockService(StockRepository repository) {
        this.repository = repository;
    }

    public void addStock(int productId, int warehouseId, int qty) {
        StockKey key = new StockKey(productId,warehouseId);

        StockItem stockItem = repository
                .findByKey(key)
                .orElseGet(() -> new StockItem(productId, warehouseId, 0, 0));
        stockItem.addStock(qty);
        repository.save(stockItem);
    }

    public void reserveStock(int productId, int warehouseId, int qty) {
        StockKey key = new StockKey(productId,warehouseId);

        StockItem stockItem = repository
                .findByKey(key)
                .orElseThrow(() -> new IllegalStateException("stock not found"));
        stockItem.reserve(qty);
        repository.save(stockItem);
    }

    public void releaseStock(int productId, int warehouseId, int qty) {
        StockKey key = new StockKey(productId,warehouseId);
        StockItem stockItem = repository
                .findByKey(key)
                .orElseThrow(() -> new IllegalStateException("stock not found"));
        stockItem.releaseReservation(qty);
        repository.save(stockItem);
    }

    public void reserveOrder(int warehouseId, Order order){
        List<OrderLine> items = order.getItems();
        for (OrderLine line : items){
            reserveStock(line.getProductId(), warehouseId, line.getQuantity());
        }
        order.reserve();
    }
    public void releaseOrderReservation(int warehouseId, Order order){
        List<OrderLine> items = order.getItems();
        for (OrderLine line : items){
            releaseStock(line.getProductId(), warehouseId, line.getQuantity());
        }
        order.cancel();
    }

}