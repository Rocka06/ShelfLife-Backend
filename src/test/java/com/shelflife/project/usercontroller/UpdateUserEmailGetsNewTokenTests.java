package com.shelflife.project.usercontroller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UpdateUserEmailGetsNewTokenTests {
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
    void getsNewTokenAsUser() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        MvcResult res = mockMvc.perform(patch("/api/users/" + testUser.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"newEmail@test.test\"}"))
                .andExpect(status().isOk()).andReturn();

        // Invalidated
        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isForbidden());

        Cookie newJwt = res.getResponse().getCookie("jwt");
        assertNotNull(newJwt);
        assertNotEquals(jwt, newJwt.getValue());

        jwtCookie.setValue(newJwt.getValue());
        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk());
    }

    @Test
    void getsNewTokenAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        MvcResult res = mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"newEmail@test.test\"}"))
                .andExpect(status().isOk()).andReturn();

        // Invalidated
        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isForbidden());

        Cookie newJwt = res.getResponse().getCookie("jwt");
        assertNotNull(newJwt);
        assertNotEquals(jwt, newJwt.getValue());

        jwtCookie.setValue(newJwt.getValue());
        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk());
    }

    @Test
    void doesNotGetNewTokenAsAdmin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        MvcResult res = mockMvc.perform(patch("/api/users/" + testUser.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"newEmail@test.test\"}"))
                .andExpect(status().isOk()).andReturn();

        assertNull(res.getResponse().getCookie("jwt"));

        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk());
    }
}
