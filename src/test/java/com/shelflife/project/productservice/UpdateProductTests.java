package com.shelflife.project.productservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.shelflife.project.dto.UpdateProductRequest;
import com.shelflife.project.exception.BarcodeExistsException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.service.ProductService;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UpdateProductTests {
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
    void throwsAccessDeniedAsAnonymous() {
        when(userService.getUserByAuth(auth)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> productService.updateProduct(1L, emptyRequest(), auth));
        verifyNoInteractions(repo);
    }

    @Test
    void updateFailsOnNonOwnedProduct() {
        User current = testUser(1L, false);
        Product productToUpdate = testProduct(1L, 2L);

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.findById(1L)).thenReturn(Optional.of(productToUpdate));

        assertThrows(AccessDeniedException.class, () -> productService.updateProduct(1L, emptyRequest(), auth));
        verify(repo, never()).save(any());
    }

    @Test
    void throwsBarcodeExists() {
        User current = testUser(1L, false);
        Product p = testProduct(1L, 1L);
        UpdateProductRequest req = new UpdateProductRequest();
        req.setBarcode("12345");

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(productService.existsByBarcode("12345")).thenReturn(true);

        assertThrows(BarcodeExistsException.class, () -> productService.updateProduct(1L, req, auth));
        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForInvalidExpiration() {
        User current = testUser(1L, false);
        Product p = testProduct(1L, 1L);
        UpdateProductRequest req = new UpdateProductRequest();
        req.setExpirationDaysDelta(0);

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(1L, req, auth));
        verify(repo, never()).save(any());
    }

    @Test
    void throwsIllegalArgumentForInvalidRunningLow() {
        User current = testUser(1L, false);
        Product p = testProduct(1L, 1L);
        UpdateProductRequest req = new UpdateProductRequest();
        req.setRunningLow(0);

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(1L, req, auth));
        verify(repo, never()).save(any());
    }

    @Test
    void adminCanUpdateAny() {
        User current = testUser(1L, true);
        Product p = testProduct(1L, 2L);
        UpdateProductRequest req = new UpdateProductRequest();
        req.setRunningLow(21);

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        assertDoesNotThrow(() -> productService.updateProduct(1L, req, auth));
        verify(repo).save(p);
    }

    @Test
    void userCanUpdateOwned() {
        User current = testUser(1L, false);
        Product p = testProduct(1L, 1L);
        UpdateProductRequest req = new UpdateProductRequest();
        req.setExpirationDaysDelta(21);

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.findById(1L)).thenReturn(Optional.of(p));

        assertDoesNotThrow(() -> productService.updateProduct(1L, req, auth));
        verify(repo).save(p);
    }

    @Test
    void productIsUpdated() {
        User current = testUser(1L, false);
        Product p = testProduct(1L, 1L);
        UpdateProductRequest req = new UpdateProductRequest();
        req.setName("testName");
        req.setCategory("newCat");
        req.setExpirationDaysDelta(21);
        req.setRunningLow(10);
        req.setBarcode("6789");

        when(userService.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product newP = productService.updateProduct(1L, req, auth);
        assertEquals("testName", newP.getName());
        assertEquals("newCat", newP.getCategory());
        assertEquals("6789", newP.getBarcode());
        assertEquals(10, newP.getRunningLow());
        assertEquals(21, newP.getExpirationDaysDelta());
        assertEquals(1, newP.getId());
        assertEquals(1, newP.getOwnerId());
    }

    private User testUser(long id, boolean admin) {
        User u = new User();
        u.setId(id);
        u.setAdmin(admin);
        u.setEmail("test" + id + "@test.test");
        u.setUsername("test" + id);
        return u;
    }

    private Product testProduct(long id, long ownerid) {
        Product p = new Product();
        p.setId(ownerid);
        p.setOwnerId(ownerid);
        p.setName("name");
        p.setCategory("cat");
        p.setBarcode("12345");
        p.setExpirationDaysDelta(2);
        p.setRunningLow(2);

        return p;
    }

    private UpdateProductRequest emptyRequest() {
        return new UpdateProductRequest();
    }
}
