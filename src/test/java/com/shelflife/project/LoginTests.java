package com.shelflife.project;

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

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LoginTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

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
    void loginSuccessful() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getLoginJson("test@test.test", "test123")))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andReturn();

        String jwt = result.getResponse().getCookie("jwt").getValue();
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void loginInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getLoginJson("test@Test.test", "test123")))
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist("jwt"))
                .andReturn();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void loginInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getLoginJson("test@test.test", "test1234")))
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist("jwt"))
                .andReturn();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void loginInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist("jwt"))
                .andReturn();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void cantLoginMultipleTimes() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getLoginJson("test@test.test", "test123")))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("jwt"))
                .andReturn();

        String jwt = result.getResponse().getCookie("jwt").getValue();
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(get("/api/auth/me")
                .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(post("/api/auth/login")
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getLoginJson("test@test.test", "test123")))
                .andExpect(status().isBadRequest());
    }

    String getLoginJson(String email, String password) {
        return "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
    }
}
