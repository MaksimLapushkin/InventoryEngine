package com.inventory.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class giInventoryEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryEngineApplication.class, args);
    }
}
