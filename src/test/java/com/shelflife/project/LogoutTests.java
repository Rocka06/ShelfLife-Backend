package com.shelflife.project;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.shelflife.project.model.InvalidJwt;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.InvalidJwtRepository;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.security.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LogoutTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvalidJwtRepository invalidJwtRepository;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String token;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setEmail("test@test.test");
        testUser.setUsername("test");
        testUser.setPassword("test123");
        userRepository.save(testUser);

        token = jwtService.generateToken(testUser.getEmail());
    }

    @Test
    void logoutSuccessfulWithCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie[] { new Cookie("jwt", token) }))
                .andExpect(status().isOk());

        Optional<InvalidJwt> jwt = invalidJwtRepository.findByToken(token);
        assertTrue(jwt.isPresent());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutSuccessfulWithBearer() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Optional<InvalidJwt> jwt = invalidJwtRepository.findByToken(token);
        assertTrue(jwt.isPresent());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cantLogoutMultipleTimes() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Optional<InvalidJwt> jwt = invalidJwtRepository.findByToken(token);
        assertTrue(jwt.isPresent());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error").exists());
    }
}
