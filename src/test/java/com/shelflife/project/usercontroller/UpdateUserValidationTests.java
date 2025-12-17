package com.shelflife.project.usercontroller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.shelflife.project.model.User;
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
public class UpdateUserValidationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    private User testAdmin;
    private User testUser;

    @BeforeEach
    void setup() {
        testAdmin = new User();
        testAdmin.setEmail("test@test.test");
        testAdmin.setUsername("test");
        testAdmin.setPassword(encoder.encode("test123"));
        testAdmin.setAdmin(true);
        userRepository.save(testAdmin);

        testUser = new User();
        testUser.setEmail("testuser@test.test");
        testUser.setUsername("testuser");
        testUser.setPassword(encoder.encode("test123"));
        testUser.setAdmin(false);
        userRepository.save(testUser);
    }

    // Email
    @Test
    void emailAlreadyUsedError() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"testuser@test.test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.username").doesNotExist());
    }

    @Test
    void invalidEmailError() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"testusertest.test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.username").doesNotExist());

        assertFalse(userRepository.existsByEmail("testusertest.test"));
    }

    @Test
    void emptyEmailError() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.username").doesNotExist());

        assertFalse(userRepository.existsByEmail(""));
    }

    // Username
    @Test
    void longUsernameError() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"username\":\"test username that is too long for a request\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").exists());

        assertEquals(testAdmin.getUsername(), userRepository.findByEmail(testAdmin.getEmail()).get().getUsername());
    }

    @Test
    void emptyUsernameError() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"username\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").exists());

        assertEquals(testAdmin.getUsername(), userRepository.findByEmail(testAdmin.getEmail()).get().getUsername());
    }
}
