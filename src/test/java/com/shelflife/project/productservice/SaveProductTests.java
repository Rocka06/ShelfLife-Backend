package com.shelflife.project.productservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.dto.CreateProductRequest;
import com.shelflife.project.exception.BarcodeExistsException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.service.ProductService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class SaveProductTests {
    @Mock
    ProductRepository repo;

    @Mock
    UserService userService;

    @Mock
    Authentication auth;

    @Spy
    @InjectMocks
    ProductService productService;

    @Test
    void successfulCreation() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(testUser(1, false)));

        assertDoesNotThrow(() -> {
            productService.saveProduct(validRequest(), auth);
        });

        verify(repo).save(any(Product.class));
    }

    @Test
    void throwsAccessDeniedAsAnonymous() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> productService.saveProduct(validRequest(), auth));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsBarcodeExists() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(testUser(1, false)));
        when(productService.existsByBarcode(validRequest().getBarcode())).thenReturn(true);

        assertThrows(BarcodeExistsException.class, () -> {
            productService.saveProduct(validRequest(), auth);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForNullCategory() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(testUser(1, false)));

        CreateProductRequest request = validRequest();
        request.setCategory(null);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, auth);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForEmptyCategory() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(testUser(1, false)));

        CreateProductRequest request = validRequest();
        request.setCategory("");

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, auth);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForNullName() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(testUser(1, false)));

        CreateProductRequest request = validRequest();
        request.setName(null);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, auth);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForEmptyName() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(testUser(1, false)));

        CreateProductRequest request = validRequest();
        request.setName("");

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, auth);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForZeroRunningLow() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(testUser(1, false)));

        CreateProductRequest request = validRequest();
        request.setRunningLow(0);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, auth);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForNegativeRunningLow() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(testUser(1, false)));

        CreateProductRequest request = validRequest();
        request.setRunningLow(-5);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, auth);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForZeroExpiration() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(testUser(1, false)));

        CreateProductRequest request = validRequest();
        request.setExpirationDaysDelta(0);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, auth);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForNegativeExpiration() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(testUser(1, false)));

        CreateProductRequest request = validRequest();
        request.setExpirationDaysDelta(-5);

        assertThrows(IllegalArgumentException.class, () -> {
            productService.saveProduct(request, auth);
        });

        verify(repo, never()).save(any());
    }

    private CreateProductRequest validRequest() {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("ProductName");
        req.setRunningLow(5);
        req.setExpirationDaysDelta(365);
        req.setCategory("Canned soup");
        req.setBarcode("12346");
        return req;
    }

    private User testUser(long id, boolean admin) {
        User u = new User();
        u.setId(id);
        u.setAdmin(admin);
        u.setEmail("test" + id + "@test.test");
        u.setUsername("test" + id);
        return u;
    }

}
