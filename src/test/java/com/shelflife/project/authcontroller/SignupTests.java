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
import org.springframework.transaction.annotation.Transactional;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.JwtService;

import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SignupTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setEmail("test@test.test");
        testUser.setUsername("test");
        testUser.setPassword(encoder.encode("test123"));
        userRepository.save(testUser);
    }

    @Test
    void signupSuccessful() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"test1@test.test\", \"username\":\"test1\", \"password\":\"Test123\", \"passwordRepeat\":\"Test123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test1@test.test"))
                .andExpect(jsonPath("$.username").value("test1"))
                .andExpect(jsonPath("$.isAdmin").value("false"))
                .andExpect(jsonPath("$.id").isNumber());

        assertTrue(userRepository.existsByEmail("test1@test.test"));
    }

    // Email
    @Test
    void emailAlreadyUsedError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"test@test.test\", \"username\":\"test1\", \"password\":\"Test123\", \"passwordRepeat\":\"Test123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordRepeat").doesNotExist());
    }

    @Test
    void invalidEmailError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"test\", \"username\":\"test1\", \"password\":\"Test123\", \"passwordRepeat\":\"Test123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordRepeat").doesNotExist());

        assertFalse(userRepository.existsByEmail("test"));
    }

    @Test
    void emptyEmailError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"\", \"username\":\"test1\", \"password\":\"Test123\", \"passwordRepeat\":\"Test123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordRepeat").doesNotExist());

        assertFalse(userRepository.existsByEmail(""));
    }

    @Test
    void nullEmailError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test1\", \"password\":\"Test123\", \"passwordRepeat\":\"Test123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordRepeat").doesNotExist());
    }

    // Username
    @Test
    void longUsernameError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"test1@test.test\", \"username\":\"test username that is too long for a request\", \"password\":\"Test123\", \"passwordRepeat\":\"Test123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordRepeat").doesNotExist());

        assertFalse(userRepository.existsByEmail("test1@test.test"));
    }

    @Test
    void emptyUsernameError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"test1@test.test\", \"username\":\"\", \"password\":\"Test123\", \"passwordRepeat\":\"Test123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordRepeat").doesNotExist());

        assertFalse(userRepository.existsByEmail("test1@test.test"));
    }

    @Test
    void nullUsernameError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test1@test.test\", \"password\":\"Test123\", \"passwordRepeat\":\"Test123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordRepeat").doesNotExist());

        assertFalse(userRepository.existsByEmail("test1@test.test"));
    }

    // Password
    @Test
    void shortPasswordError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"test1@test.test\", \"username\":\"test\", \"password\":\"test\", \"passwordRepeat\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.passwordRepeat").doesNotExist());

        assertFalse(userRepository.existsByEmail("test1@test.test"));
    }

    @Test
    void emptyPasswordError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"test1@test.test\", \"username\":\"test\", \"password\":\"\", \"passwordRepeat\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.passwordRepeat").exists());

        assertFalse(userRepository.existsByEmail("test1@test.test"));
    }

    @Test
    void nullPasswordError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test1@test.test\", \"username\":\"test\", \"passwordRepeat\":\"Test123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.passwordRepeat").doesNotExist());

        assertFalse(userRepository.existsByEmail("test1@test.test"));
    }

    @Test
    void passwordNotSameError() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"test1@test.test\", \"username\":\"test\", \"password\":\"test1234\", \"passwordRepeat\":\"test123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordRepeat").exists());

        assertFalse(userRepository.existsByEmail("test1@test.test"));
    }

    @Test
    void cantSignupWhileLoggedIn() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.isAdmin").value(testUser.isAdmin()));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(jwtCookie)
                .content(
                        "{\"email\":\"test1@test.test\", \"username\":\"test1\", \"password\":\"Test123\", \"passwordRepeat\":\"Test123\"}"))
                .andExpect(status().isForbidden());
    }
}
