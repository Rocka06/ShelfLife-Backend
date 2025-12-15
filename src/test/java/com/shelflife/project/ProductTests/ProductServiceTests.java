package com.shelflife.project.ProductTests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.InvalidParameterException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import com.shelflife.project.exceptions.BarcodeExistsException;
import com.shelflife.project.exceptions.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.service.ProductService;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductServiceTests {
    @Autowired
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setup() {
        testProduct = new Product();
        testProduct.setName("testProduct");
        testProduct.setCategory("test");
        testProduct.setOwnerId(1);
        testProduct.setBarcode("12345");
        testProduct.setExpirationDaysDelta(10);
        testProduct.setRunningLow(2);

        testProduct = productService.saveProduct(testProduct);
    }

    // Save
    @Test
    void productSavedSuccessfully() {
        Product product = new Product();
        product.setName("otherTestProduct");
        product.setCategory("test1");
        product.setOwnerId(1);
        product.setBarcode("123456");
        product.setExpirationDaysDelta(10);
        product.setRunningLow(2);

        Product savedProduct = productService.saveProduct(product);

        assertTrue(productService.productExistsByID(savedProduct.getId()));
        assertEquals(2, productService.getAllProducts().size());
    }

    @Test
    void cantAddProductWithSameBarcode() {
        Product product = new Product();
        product.setName("otherTestProduct");
        product.setCategory("test1");
        product.setOwnerId(1);
        product.setBarcode("12345");
        product.setExpirationDaysDelta(10);
        product.setRunningLow(2);

        assertThrows(BarcodeExistsException.class, () -> {
            productService.saveProduct(product);
        });

        assertEquals(1, productService.getAllProducts().size());
    }

    @Test
    void emptyNameErrorOnSave() {
        Product product = new Product();
        product.setName("");
        product.setCategory("test1");
        product.setOwnerId(1);
        product.setBarcode("123456");
        product.setExpirationDaysDelta(10);
        product.setRunningLow(2);

        assertThrows(InvalidParameterException.class, () -> {
            productService.saveProduct(product);
        });

        assertEquals(1, productService.getAllProducts().size());
    }

    @Test
    void emptyCategoryErrorOnSave() {
        Product product = new Product();
        product.setName("otherTestProduct");
        product.setCategory("");
        product.setOwnerId(1);
        product.setBarcode("123456");
        product.setExpirationDaysDelta(10);
        product.setRunningLow(2);

        assertThrows(InvalidParameterException.class, () -> {
            productService.saveProduct(product);
        });

        assertEquals(1, productService.getAllProducts().size());
    }

    // Update
    @Test
    void updateProductAsAdmin() {
        User admin = new User();
        admin.setId(10);
        admin.setAdmin(true);

        testProduct.setName("edited");

        assertDoesNotThrow(() -> {
            productService.updateProduct(testProduct.getId(), testProduct, admin);
        });

        Product result = productService.getProductByID(testProduct.getId());

        assertEquals("edited", result.getName());
        assertEquals("test", result.getCategory());
        assertEquals("12345", result.getBarcode());
        assertEquals(10, result.getExpirationDaysDelta());
        assertEquals(2, result.getRunningLow());
    }

    @Test
    void updateProductAsOwner() {
        User admin = new User();
        admin.setId(1);
        admin.setAdmin(false);

        testProduct.setName("edited");

        assertDoesNotThrow(() -> {
            productService.updateProduct(testProduct.getId(), testProduct, admin);
        });

        Product result = productService.getProductByID(testProduct.getId());

        assertEquals("edited", result.getName());
        assertEquals("test", result.getCategory());
        assertEquals("12345", result.getBarcode());
        assertEquals(10, result.getExpirationDaysDelta());
        assertEquals(2, result.getRunningLow());
    }

    @Test
    void updateProductFailsWithoutOwnership() {
        User admin = new User();
        admin.setId(10);
        admin.setAdmin(false);

        testProduct.setName("edited");

        assertThrows(AccessDeniedException.class, () -> {
            productService.updateProduct(testProduct.getId(), testProduct, admin);
        });

        Product result = productService.getProductByID(testProduct.getId());

        assertEquals("edited", result.getName());
        assertEquals("test", result.getCategory());
        assertEquals("12345", result.getBarcode());
        assertEquals(10, result.getExpirationDaysDelta());
        assertEquals(2, result.getRunningLow());
    }

    // Remove
    @Test
    void successfulRemoveAsOwner() {
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setAdmin(false);

        assertDoesNotThrow(() -> {
            productService.removeProduct(testProduct.getId(), mockUser);
        });

        assertEquals(0, productService.getAllProducts().size());
    }

    @Test
    void successfulRemoveAsAdmin() {
        User mockUser = new User();
        mockUser.setId(10);
        mockUser.setAdmin(true);

        assertDoesNotThrow(() -> {
            productService.removeProduct(testProduct.getId(), mockUser);
        });

        assertEquals(0, productService.getAllProducts().size());
    }

    @Test
    void removeWithInvalidId() {
        User mockUser = new User();
        mockUser.setId(10);
        mockUser.setAdmin(true);

        assertThrows(ItemNotFoundException.class, () -> {
            productService.removeProduct(testProduct.getId() + 1, mockUser);
        });

        assertEquals(1, productService.getAllProducts().size());
    }

    @Test
    void removeWithoutOwnership() {
        User mockUser = new User();
        mockUser.setId(10);

        assertThrows(AccessDeniedException.class, () -> {
            productService.removeProduct(testProduct.getId(), mockUser);
        });

        assertEquals(1, productService.getAllProducts().size());
    }

    // Get
    @Test
    void testGetAllProducts() {
        assertEquals(1, productService.getAllProducts().size());

        Product product = new Product();
        product.setName("otherTestProduct");
        product.setCategory("test1");
        product.setOwnerId(1);
        product.setBarcode("123456");
        product.setExpirationDaysDelta(10);
        product.setRunningLow(2);

        productService.saveProduct(product);

        assertEquals(2, productService.getAllProducts().size());
        assertEquals("otherTestProduct", productService.getAllProducts().get(1).getName());
    }

    @Test
    void testGetProductById() {
        assertThrows(ItemNotFoundException.class, () -> {
            productService.getProductByID(0);
        });

        assertThrows(ItemNotFoundException.class, () -> {
            productService.getProductByID(-1);
        });

        assertDoesNotThrow(() -> {
            productService.getProductByID(testProduct.getId());
        });

        assertEquals("testProduct", productService.getProductByID(testProduct.getId()).getName());
    }

    @Test
    void testGetProductByBarcode() {
        assertThrows(ItemNotFoundException.class, () -> {
            productService.getProductByBarcode("5678");
        });

        assertThrows(ItemNotFoundException.class, () -> {
            productService.getProductByBarcode("0");
        });

        assertDoesNotThrow(() -> {
            productService.getProductByBarcode("12345");
        });

        assertEquals("testProduct", productService.getProductByBarcode(testProduct.getBarcode()).getName());
    }

    @Test
    void testGetProductsByCategory() {
        assertEquals(0, productService.getProductsByCategory("5678").size());
        assertEquals(1, productService.getProductsByCategory("test").size());
        assertEquals("testProduct", productService.getProductsByCategory("test").get(0).getName());
    }

    @Test
    void testGetCategories() {
        assertEquals(1, productService.getCategories().size());
        assertEquals("test", productService.getCategories().get(0));

        Product product = new Product();
        product.setName("otherTestProduct");
        product.setCategory("test1");
        product.setOwnerId(1);
        product.setBarcode("123456");
        product.setExpirationDaysDelta(10);
        product.setRunningLow(2);

        productService.saveProduct(product);

        assertEquals(2, productService.getCategories().size());
        assertEquals("test1", productService.getCategories().get(1));
    }

}
