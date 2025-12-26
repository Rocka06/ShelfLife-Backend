package com.shelflife.project.authcontroller;

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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PasswordChangeTests {
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

    @Test
    void changeSuccessfulAsUser() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/auth/password")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"oldPassword\":\"test123\", \"newPassword\":\"test1234\", \"newPasswordRepeat\":\"test1234\"}"))
                .andExpect(status().isOk());

        User user = userRepository.findByEmail(testUser.getEmail()).get();
        assertTrue(encoder.matches("test1234", user.getPassword()));
    }

    @Test
    void changeSuccessfulAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/auth/password")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"oldPassword\":\"test123\", \"newPassword\":\"test1234\", \"newPasswordRepeat\":\"test1234\"}"))
                .andExpect(status().isOk());

        User user = userRepository.findByEmail(testAdmin.getEmail()).get();
        assertTrue(encoder.matches("test1234", user.getPassword()));
    }

    @Test
    void invalidOldPassword() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/auth/password")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"oldPassword\":\"test1235\", \"newPassword\":\"test1234\", \"newPasswordRepeat\":\"test1234\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.oldPassword").exists());

        User user = userRepository.findByEmail(testAdmin.getEmail()).get();
        assertTrue(encoder.matches("test123", user.getPassword()));
    }

    @Test
    void passwordsDontMatch() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(post("/api/auth/password")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"oldPassword\":\"test123\", \"newPassword\":\"test1234\", \"newPasswordRepeat\":\"test12345\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.newPasswordRepeat").exists());

        User user = userRepository.findByEmail(testAdmin.getEmail()).get();
        assertTrue(encoder.matches("test123", user.getPassword()));
    }
}