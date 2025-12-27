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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class GetProductTests {
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
    void getProductAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/products/" + testProduct.getId())
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.name").value(testProduct.getName()))
                .andExpect(jsonPath("$.barcode").value(testProduct.getBarcode()))
                .andExpect(jsonPath("$.category").value(testProduct.getCategory()))
                .andExpect(jsonPath("$.ownerId").value(testProduct.getOwnerId()))
                .andExpect(jsonPath("$.runningLow").value(testProduct.getRunningLow()))
                .andExpect(jsonPath("$.expirationDaysDelta").value(testProduct.getExpirationDaysDelta()));
    }

    @Test
    void getProductAsUser() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/products/" + testProduct.getId())
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.name").value(testProduct.getName()))
                .andExpect(jsonPath("$.barcode").value(testProduct.getBarcode()))
                .andExpect(jsonPath("$.category").value(testProduct.getCategory()))
                .andExpect(jsonPath("$.ownerId").value(testProduct.getOwnerId()))
                .andExpect(jsonPath("$.runningLow").value(testProduct.getRunningLow()))
                .andExpect(jsonPath("$.expirationDaysDelta").value(testProduct.getExpirationDaysDelta()));
    }

    @Test
    void returnsNotFound() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/products/" + testProduct.getId() + 1)
                .cookie(jwtCookie))
                .andExpect(status().isNotFound());
    }

    @Test
    void cantGetProductAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/products/" + testProduct.getId()))
                .andExpect(status().isForbidden());
    }
}
