import model.*;
import repository.*;
import service.*;
import util.InventoryReportService;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        // repository init
        ProductRepository productRepo = new InMemoryProductRepository();
        StockRepository stockRepo = new InMemoryStockRepository();

        // Services init
        ProductService productService = new ProductService(productRepo);
        StockService stockService = new StockService(stockRepo);

        // model.Warehouse init
        Warehouse warehouse = new Warehouse(1, "Main warehouse");

        // Add products to use
        Product milk = productService.addProduct("MILK-01", "Milk", Unit.PIECE);
        Product bread = productService.addProduct("BREAD-01", "Bread", Unit.PIECE);
        Product apple = productService.addProduct("APPLE-01", "Apple", Unit.KG);

        // add products to warehouse
        stockService.addStock(milk.getId(), warehouse.getId(), 20);
        stockService.addStock(bread.getId(), warehouse.getId(), 15);
        stockService.addStock(apple.getId(), warehouse.getId(), 30);

        // create order
        OrderLine line1 = new OrderLine(milk.getId(), 2);
        OrderLine line2 = new OrderLine(apple.getId(), 3);

        Order order = new Order(1, List.of(line1, line2));

        // reserve order
        stockService.reserveOrder(1, order);
        System.out.println("model.Order status: " + order.getStatus());


        List<StockItem> stockItems = stockRepo.findAll();

        for (StockItem item : stockItems) {
            System.out.println(
                    "model.Product " + item.getProductId() +
                            " available=" + item.getAvailable() +
                            " reserved=" + item.getReserved()
            );
        }
        InventoryReportService reportService =
                new InventoryReportService(stockRepo);

        reportService.writeStockReport("report.txt");
    }
}