package com.shelflife.project.productcontroller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DeleteProductTests {
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

        testProduct = new Product();
        testProduct.setName("Chips");
        testProduct.setOwnerId(testUser.getId());
        testProduct.setRunningLow(2);
        testProduct.setExpirationDaysDelta(200);
        testProduct.setCategory("Snack");
        testProduct.setBarcode("12345");
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void deleteProductAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(delete("/api/products/" + testProduct.getId())
                .cookie(jwtCookie))
                .andExpect(status().isOk());

        assertTrue(productRepository.findById(testProduct.getId()).isEmpty());
    }

    @Test
    void deleteOwnedAsUser() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(delete("/api/products/" + testProduct.getId())
                .cookie(jwtCookie))
                .andExpect(status().isOk());

        assertTrue(productRepository.findById(testProduct.getId()).isEmpty());
    }

    @Test
    void cantDeleteNonOwnedAsUser() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        Product p = new Product();
        p.setOwnerId(testAdmin.getId());
        p.setName("test");
        productRepository.save(p);

        mockMvc.perform(delete("/api/products/" + p.getId())
                .cookie(jwtCookie))
                .andExpect(status().isForbidden());

        assertTrue(productRepository.findById(p.getId()).isPresent());
    }

    @Test
    void returnsNotFound() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(delete("/api/products/" + testProduct.getId() + 1)
                .cookie(jwtCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    void cantDeleteAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/products/" + testProduct.getId()))
                .andExpect(status().isForbidden());

        assertTrue(productRepository.findById(testProduct.getId()).isPresent());
    }
}
