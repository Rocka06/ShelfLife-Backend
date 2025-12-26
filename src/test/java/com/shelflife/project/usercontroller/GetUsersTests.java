package com.shelflife.project.usercontroller;

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

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class GetUsersTests {
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

    // Users
    @Test
    void getUsersSuccessfulAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/users")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].email").value(testAdmin.getEmail()))
                .andExpect(jsonPath("$[0].username").value(testAdmin.getUsername()))
                .andExpect(jsonPath("$[0].isAdmin").value(true));
    }

    @Test
    void cantGetUsersAsUser() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/users")
                .cookie(jwtCookie))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    void cantGetUsersAsGuest() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }
}
