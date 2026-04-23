package com.maxlapushkin.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryEngineOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("InventoryEngine API")
                        .description("Backend-style inventory management API with atomic stock reservation")
                        .version("v1")
                        .contact(new Contact().name("InventoryEngine Team")));
    }
}
