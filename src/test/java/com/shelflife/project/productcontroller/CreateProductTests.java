package com.shelflife.project.productcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CreateProductTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductRepository productRepository;

    private User testAdmin;
    private User testUser;

    private Product testProduct;

    @BeforeEach
    void setup() {
        testAdmin = new User();
        testAdmin.setEmail("test@test.test");
        testAdmin.setUsername("test");
        testAdmin.setPassword(encoder.encode("test123"));
        testAdmin.setAdmin(true);
        testAdmin = userRepository.save(testAdmin);

        testUser = new User();
        testUser.setEmail("testuser@test.test");
        testUser.setUsername("testuser");
        testUser.setPassword(encoder.encode("test123"));
        testUser.setAdmin(false);
        testUser = userRepository.save(testUser);

        Product p = new Product();
        p.setName("Chips");
        p.setCategory("Snack");
        p.setBarcode("12345");
        p.setOwnerId(testUser.getId());
        p.setRunningLow(2);
        p.setExpirationDaysDelta(200);
        testProduct = productRepository.save(p);
    }

    @Test
    void forbiddenAsAnonymous() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void validRequestAsUser() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/products")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getJson("Test", "test", "6789", 1, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(testProduct.getId() + 1))
                .andExpect(jsonPath("ownerId").value(testUser.getId()))
                .andExpect(jsonPath("name").value("Test"))
                .andExpect(jsonPath("category").value("test"))
                .andExpect(jsonPath("barcode").value("6789"))
                .andExpect(jsonPath("expirationDaysDelta").value(1))
                .andExpect(jsonPath("runningLow").value(1));

        assertTrue(productRepository.existsById(testProduct.getId() + 1));
    }

    @Test
    void validRequestAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/products")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getJson("Test", "test", "6789", 1, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(testProduct.getId() + 1))
                .andExpect(jsonPath("ownerId").value(testAdmin.getId()))
                .andExpect(jsonPath("name").value("Test"))
                .andExpect(jsonPath("category").value("test"))
                .andExpect(jsonPath("barcode").value("6789"))
                .andExpect(jsonPath("expirationDaysDelta").value(1))
                .andExpect(jsonPath("runningLow").value(1));

        assertTrue(productRepository.existsById(testProduct.getId() + 1));
    }

    @Test
    void requestIsStillValidWithoutBarcode() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/products")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getJson("Test", "test", "", 1, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(testProduct.getId() + 1))
                .andExpect(jsonPath("ownerId").value(testAdmin.getId()))
                .andExpect(jsonPath("name").value("Test"))
                .andExpect(jsonPath("category").value("test"))
                .andExpect(jsonPath("barcode").isEmpty())
                .andExpect(jsonPath("expirationDaysDelta").value(1))
                .andExpect(jsonPath("runningLow").value(1));

        assertTrue(productRepository.existsById(testProduct.getId() + 1));
    }

    @Test
    void emptyNameThrowsError() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/products")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getJson("", "test", "6789", 1, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("name").exists());

        assertFalse(productRepository.existsById(testProduct.getId() + 1));
    }

    @Test
    void emptyCategoryThrowsError() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/products")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getJson("Test", "", "6789", 1, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("category").exists());

        assertFalse(productRepository.existsById(testProduct.getId() + 1));
    }

    @Test
    void throwsBarcodeExists() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/products")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getJson("Test", "test", "12345", 1, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("barcode").exists());

        assertFalse(productRepository.existsById(testProduct.getId() + 1));
    }

    @Test
    void invalidExpiration() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/products")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getJson("Test", "test", "6789", 0, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("expirationDaysDelta").exists());

        assertFalse(productRepository.existsById(testProduct.getId() + 1));
    }

    @Test
    void ivalidRunningLow() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/products")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getJson("Test", "test", "6789", 1, 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("runningLow").exists());

        assertFalse(productRepository.existsById(testProduct.getId() + 1));
    }

    private String getJson(String name, String category, String barcode, long expirationDaysDelta, long runningLow) {
        return "{\"name\":\"" + name + "\", \"category\":\"" + category + "\", \"barcode\":\"" + barcode
                + "\", \"expirationDaysDelta\":" + expirationDaysDelta + ", \"runningLow\":" + runningLow + "}";
    }
}
