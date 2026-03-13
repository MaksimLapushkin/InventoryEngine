import java.util.List;
import java.util.Optional;

public interface StockRepository {
    void save(StockItem item);
    Optional<StockItem> findByKey(StockKey key);
    List<StockItem> findAll();
}
