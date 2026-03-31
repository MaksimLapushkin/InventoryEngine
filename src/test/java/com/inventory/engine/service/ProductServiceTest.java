package com.inventory.engine.service;

import com.inventory.engine.model.Product;
import com.inventory.engine.model.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.inventory.engine.repository.InMemoryProductRepository;
import com.inventory.engine.repository.ProductRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductService tests")
class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepository();
        productService = new ProductService(productRepository);
    }

    @Nested
    @DisplayName("addProduct()")
    class AddProductTests {

        @Test
        void shouldAddProductSuccessfully() {
            Product product = productService.addProduct("SKU-1", "Milk", Unit.PIECE);

            assertThat(product).isNotNull();
            assertThat(product.getId()).isEqualTo(0);
            assertThat(product.getSku()).isEqualTo("SKU-1");
            assertThat(product.getName()).isEqualTo("Milk");
            assertThat(product.getUnit()).isEqualTo(Unit.PIECE);
        }

        @Test
        void shouldAssignIncreasingIdsToNewProducts(){
            productService.addProduct("SKU-1", "Milk", Unit.LITER);
            productService.addProduct("SKU-2", "Bread", Unit.PIECE);

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
            Product created = productService.addProduct("SKU-1", "Milk", Unit.LITER);

            Product found = productService.getProduct(created.getId());

            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(created.getId());
            assertThat(found.getSku()).isEqualTo("SKU-1");
            assertThat(found.getName()).isEqualTo("Milk");
            assertThat(found.getUnit()).isEqualTo(Unit.LITER);
        }

        @Test
        void shouldThrowExceptionWhenProductNotFound() {
            assertThatThrownBy(() -> productService.getProduct(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("No such product found");
        }
    }

    @Nested
    @DisplayName("listProducts()")
    class ListProductsTests {

        @Test
        void shouldReturnAllProducts() {
            productService.addProduct("SKU-1", "Milk", Unit.LITER);
            productService.addProduct("SKU-2", "Water", Unit.LITER);

            List<Product> result = productService.listProducts();

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Product::getName)
                    .containsExactlyInAnyOrder("Milk", "Water");
        }

        @Test
        void shouldReturnEmptyListWhenNoProductsExist(){
            List<Product> result = productService.listProducts();

            assertThat(result).isEmpty();
        }

    }

    @Nested
    @DisplayName("findProductsByUnit()")
    class FindProductsByUnitTests {

        @Test
        void shouldReturnOnlyProductsWithRequestedUnit() {
            productService.addProduct("SKU-1", "Nut bar", Unit.PIECE);
            productService.addProduct("SKU-2", "Water", Unit.LITER);
            productService.addProduct("SKU-3", "Bread", Unit.PIECE);

            List<Product> result = productService.findProductsByUnit(Unit.PIECE);

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Product::getName)
                    .containsExactlyInAnyOrder("Nut bar", "Bread");
        }

        @Test
        void shouldReturnEmptyListWhenNoProductsMatchUnit(){
            productService.addProduct("SKU-1", "Milk", Unit.LITER);

            List<Product> result = productService.findProductsByUnit(Unit.PIECE);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findProductsByName()")
    class FindProductsByNameTests {

        @Test
        void shouldReturnMatchingProductsIgnoringCase() {
            productService.addProduct("SKU-1", "Milk", Unit.LITER);
            productService.addProduct("SKU-2", "Dark Milk Chocolate", Unit.PIECE);
            productService.addProduct("SKU-3", "Bread", Unit.PIECE);

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