package com.maxlapushkin.inventory.util;

import com.maxlapushkin.inventory.model.StockItem;
import com.maxlapushkin.inventory.repository.StockRepository;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class InventoryReportService {
    private final StockRepository stockRepository;

    public InventoryReportService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void writeStockReport(String filePath) {

        List<StockItem> items = stockRepository.findAll();

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filePath))) {

            writer.write("WAREHOUSE REPORT");
            writer.newLine();

            for (StockItem item : items) {

                String line = "product=" + item.getProductId()
                        + " available=" + item.getAvailable()
                        + " reserved=" + item.getReserved();

                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
