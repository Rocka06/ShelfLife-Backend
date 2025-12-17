package com.shelflife.project.usercontroller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
public class DeleteUserTests {
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
    void deleteUserSuccessfulAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        User user = userRepository.findByEmail(testUser.getEmail()).get();

        mockMvc.perform(delete("/api/users/" + user.getId())
                .cookie(jwtCookie))
                .andExpect(status().isOk());

        assertFalse(userRepository.findByEmail(testUser.getEmail()).isPresent());
    }

    @Test
    void adminCantDeleteItself() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        User user = userRepository.findByEmail(testAdmin.getEmail()).get();

        mockMvc.perform(delete("/api/users/" + user.getId())
                .cookie(jwtCookie))
                .andExpect(status().isForbidden());

        assertTrue(userRepository.findByEmail(testAdmin.getEmail()).isPresent());
    }

    @Test
    void deleteUserFailsAsUser() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        User user = userRepository.findByEmail(testAdmin.getEmail()).get();

        mockMvc.perform(delete("/api/users/" + user.getId())
                .cookie(jwtCookie))
                .andExpect(status().isForbidden());

        assertTrue(userRepository.findByEmail(testAdmin.getEmail()).isPresent());
    }

    @Test
    void deleteUserFailsAsGuest() throws Exception {
        User user = userRepository.findByEmail(testUser.getEmail()).get();

        mockMvc.perform(delete("/api/users/" + user.getId()))
                .andExpect(status().isForbidden());

        assertTrue(userRepository.findByEmail(testUser.getEmail()).isPresent());
    }
}
