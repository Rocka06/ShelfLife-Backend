package com.shelflife.project.usercontroller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UpdateUserAsAdminTests {
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

    // Success
    @Test
    void updateSuccessful_OnSelf_OnlyUsername() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"username\":\"testtest\"}"))
                .andExpect(status().isOk());

        User repoData = userRepository.findByEmail(testAdmin.getEmail()).get();

        assertEquals(testAdmin.getEmail(), repoData.getEmail());
        assertEquals("testtest", repoData.getUsername());
        assertEquals(testAdmin.getPassword(), repoData.getPassword());
        assertEquals(testAdmin.isAdmin(), repoData.isAdmin());
    }

    @Test
    void updateSuccessful_OnSelf_OnlyEmail() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"testuser@gmail.com\"}"))
                .andExpect(status().isOk());

        Optional<User> repoData = userRepository.findByEmail("testuser@gmail.com");

        assertTrue(repoData.isPresent());
        assertEquals(testAdmin.getUsername(), repoData.get().getUsername());
        assertEquals(testAdmin.getPassword(), repoData.get().getPassword());
        assertEquals(testAdmin.isAdmin(), repoData.get().isAdmin());
    }

    @Test
    void updateSuccessful_OnSelf_EmailAndUsername() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"testuser@gmail.com\", \"username\":\"testtest\"}"))
                .andExpect(status().isOk());

        Optional<User> repoData = userRepository.findByEmail("testuser@gmail.com");

        assertTrue(repoData.isPresent());
        assertEquals("testtest", repoData.get().getUsername());
        assertEquals(testAdmin.getPassword(), repoData.get().getPassword());
        assertEquals(testAdmin.isAdmin(), repoData.get().isAdmin());
    }

    @Test
    void updateSuccessful_OnOther() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testUser.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"username\":\"newUser\"}"))
                .andExpect(status().isOk());

        Optional<User> repoData = userRepository.findByEmail(testUser.getEmail());

        assertTrue(repoData.isPresent());
        assertEquals("newUser", repoData.get().getUsername());
        assertEquals(testUser.getPassword(), repoData.get().getPassword());
        assertEquals(testUser.isAdmin(), repoData.get().isAdmin());
    }

    @Test
    void updateSuccessful_OnOther_Admin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testUser.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"isAdmin\":\"true\"}"))
                .andExpect(status().isOk());

        Optional<User> repoData = userRepository.findByEmail(testUser.getEmail());

        assertTrue(repoData.isPresent());
        assertEquals(testUser.getUsername(), repoData.get().getUsername());
        assertEquals(testUser.getPassword(), repoData.get().getPassword());
        assertTrue(repoData.get().isAdmin());
    }

    // Fails
    @Test
    void updateFails_OnSelf_Admin() throws Exception {
        String jwt = jwtService.generateToken(testAdmin.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"isAdmin\":\"false\"}"))
                .andExpect(status().isForbidden());

        Optional<User> repoData = userRepository.findByEmail(testAdmin.getEmail());

        assertTrue(repoData.isPresent());
        assertEquals(testAdmin.getUsername(), repoData.get().getUsername());
        assertEquals(testAdmin.getPassword(), repoData.get().getPassword());
        assertTrue(repoData.get().isAdmin());
    }
}
