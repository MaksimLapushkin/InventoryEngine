package com.maxlapushkin.inventory.service;

import com.maxlapushkin.inventory.exception.ProductNotFoundException;
import com.maxlapushkin.inventory.model.Product;
import com.maxlapushkin.inventory.model.Unit;
import com.maxlapushkin.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductService tests")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void clean() {
        productRepository.deleteAll();
    }

    private String randomSku() {
        return "SKU-" + UUID.randomUUID();
    }

    @Nested
    @DisplayName("addProduct()")
    class AddProductTests {

        @Test
        void shouldAddProductSuccessfully() {
            String sku = randomSku();
            Product product = productService.addProduct(sku, "Milk", Unit.PIECE);

            assertThat(product).isNotNull();
            assertThat(product.getId()).isNotNull();
            assertThat(product.getSku()).isEqualTo(sku);
            assertThat(product.getName()).isEqualTo("Milk");
            assertThat(product.getUnit()).isEqualTo(Unit.PIECE);
        }

        @Test
        void shouldAssignIncreasingIdsToNewProducts() {
            productService.addProduct(randomSku(), "Milk", Unit.LITER);
            productService.addProduct(randomSku(), "Bread", Unit.PIECE);

            List<Product> result = productService.listProducts();

            assertThat(result.get(1).getId())
                    .isGreaterThan(result.get(0).getId());
        }
    }

    @Nested
    @DisplayName("getProduct()")
    class GetProductTests {

        @Test
        void shouldReturnProductWhenIdExists() {
            String sku = randomSku();
            Product created = productService.addProduct(sku, "Milk", Unit.LITER);

            Product found = productService.getProduct(created.getId());

            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(created.getId());
            assertThat(found.getSku()).isEqualTo(sku);
            assertThat(found.getName()).isEqualTo("Milk");
            assertThat(found.getUnit()).isEqualTo(Unit.LITER);
        }

        @Test
        void shouldThrowExceptionWhenProductNotFound() {
            assertThatThrownBy(() -> productService.getProduct(999_999L))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("listProducts()")
    class ListProductsTests {

        @Test
        void shouldReturnAllProducts() {
            productService.addProduct(randomSku(), "Milk", Unit.LITER);
            productService.addProduct(randomSku(), "Water", Unit.LITER);

            List<Product> result = productService.listProducts();

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Product::getName)
                    .containsExactlyInAnyOrder("Milk", "Water");
        }

        @Test
        void shouldReturnEmptyListWhenNoProductsExist() {
            List<Product> result = productService.listProducts();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findProductsByUnit()")
    class FindProductsByUnitTests {

        @Test
        void shouldReturnOnlyProductsWithRequestedUnit() {
            productService.addProduct(randomSku(), "Nut bar", Unit.PIECE);
            productService.addProduct(randomSku(), "Water", Unit.LITER);
            productService.addProduct(randomSku(), "Bread", Unit.PIECE);

            List<Product> result = productService.findProductsByUnit(Unit.PIECE);

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Product::getName)
                    .containsExactlyInAnyOrder("Nut bar", "Bread");
        }

        @Test
        void shouldReturnEmptyListWhenNoProductsMatchUnit() {
            productService.addProduct(randomSku(), "Milk", Unit.LITER);

            List<Product> result = productService.findProductsByUnit(Unit.PIECE);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findProductsByName()")
    class FindProductsByNameTests {

        @Test
        void shouldReturnMatchingProductsIgnoringCase() {
            productService.addProduct(randomSku(), "Milk", Unit.LITER);
            productService.addProduct(randomSku(), "Dark Milk Chocolate", Unit.PIECE);
            productService.addProduct(randomSku(), "Bread", Unit.PIECE);

            List<Product> result = productService.findProductsByName("milk");

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Product::getName)
                    .containsExactlyInAnyOrder("Milk", "Dark Milk Chocolate");
        }

        @Test
        void shouldThrowExceptionWhenNameIsBlank() {
            assertThatThrownBy(() -> productService.findProductsByName("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty name");
        }

        @Test
        void shouldThrowExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> productService.findProductsByName(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty name");
        }

        @Test
        void shouldReturnEmptyListWhenNoProductsMatchName() {
            List<Product> result = productService.findProductsByName("Pizza");

            assertThat(result).isEmpty();
        }
    }
}