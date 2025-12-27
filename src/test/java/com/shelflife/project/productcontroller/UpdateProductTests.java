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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UpdateProductTests {
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
        mockMvc.perform(patch("/api/products/" + testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"newName\"}"))
                .andExpect(status().isForbidden());

        assertEquals(testProduct.getName(), productRepository.findById(testProduct.getId()).get().getName());
    }

    @Test
    void validRequestAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/products/" + testProduct.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"newName\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(testProduct.getId()))
                .andExpect(jsonPath("ownerId").value(testProduct.getOwnerId()))
                .andExpect(jsonPath("name").value("newName"))
                .andExpect(jsonPath("category").value(testProduct.getCategory()))
                .andExpect(jsonPath("barcode").value(testProduct.getBarcode()))
                .andExpect(jsonPath("expirationDaysDelta").value(testProduct.getExpirationDaysDelta()))
                .andExpect(jsonPath("runningLow").value(testProduct.getRunningLow()));

        assertEquals("newName", productRepository.findById(testProduct.getId()).get().getName());
    }

    @Test
    void validRequestAsOwner() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/products/" + testProduct.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"newName\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(testProduct.getId()))
                .andExpect(jsonPath("ownerId").value(testProduct.getOwnerId()))
                .andExpect(jsonPath("name").value("newName"))
                .andExpect(jsonPath("category").value(testProduct.getCategory()))
                .andExpect(jsonPath("barcode").value(testProduct.getBarcode()))
                .andExpect(jsonPath("expirationDaysDelta").value(testProduct.getExpirationDaysDelta()))
                .andExpect(jsonPath("runningLow").value(testProduct.getRunningLow()));

        assertEquals("newName", productRepository.findById(testProduct.getId()).get().getName());
    }

    @Test
    void forbiddenAsNonOwner() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        Product p = new Product();
        p.setOwnerId(0);
        p.setName("test");
        productRepository.save(p);

        mockMvc.perform(patch("/api/products/" + p.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"newName\"}"))
                .andExpect(status().isForbidden());

        assertEquals("test", productRepository.findById(p.getId()).get().getName());
    }

    @Test
    void doesntSaveInvalidName() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/products/" + testProduct.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("name").exists());

        assertEquals(testProduct.getName(), productRepository.findById(testProduct.getId()).get().getName());
    }

    @Test
    void doesntSaveInvalidCategory() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/products/" + testProduct.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("category").exists());

        assertEquals(testProduct.getCategory(), productRepository.findById(testProduct.getId()).get().getCategory());
    }

    @Test
    void doesntSaveInvalidBarcode() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/products/" + testProduct.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"barcode\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("barcode").exists());

        assertEquals(testProduct.getBarcode(), productRepository.findById(testProduct.getId()).get().getBarcode());
    }

    @Test
    void doesntSaveInvalidExpiration() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/products/" + testProduct.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"expirationDaysDelta\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("expirationDaysDelta").exists());

        assertEquals(testProduct.getExpirationDaysDelta(), productRepository.findById(testProduct.getId()).get().getExpirationDaysDelta());
    }

    @Test
    void doesntSaveInvalidRunningLow() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/products/" + testProduct.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"runningLow\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("runningLow").exists());

        assertEquals(testProduct.getRunningLow(), productRepository.findById(testProduct.getId()).get().getRunningLow());
    }
}
