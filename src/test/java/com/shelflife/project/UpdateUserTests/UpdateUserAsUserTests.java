package com.shelflife.project.UpdateUserTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
public class UpdateUserAsUserTests {
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
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testUser.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"username\":\"testtest\"}"))
                .andExpect(status().isOk());

        User repoData = userRepository.findByEmail(testUser.getEmail()).get();

        assertEquals("testtest", repoData.getUsername());
        assertEquals(testUser.getEmail(), repoData.getEmail());
        assertEquals(testUser.getPassword(), repoData.getPassword());
        assertEquals(testUser.isAdmin(), repoData.isAdmin());
    }

    @Test
    void updateSuccessful_OnSelf_OnlyEmail() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testUser.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"testuser@gmail.com\"}"))
                .andExpect(status().isOk());

        Optional<User> repoData = userRepository.findByEmail("testuser@gmail.com");
        assertTrue(repoData.isPresent());
        assertEquals(testUser.getUsername(), repoData.get().getUsername());
        assertEquals(testUser.getPassword(), repoData.get().getPassword());
        assertEquals(testUser.isAdmin(), repoData.get().isAdmin());
    }

    @Test
    void updateSuccessful_OnSelf_EmailAndUsername() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testUser.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"testuser@gmail.com\", \"username\":\"testtest\"}"))
                .andExpect(status().isOk());

        Optional<User> repoData = userRepository.findByEmail("testuser@gmail.com");
        assertTrue(repoData.isPresent());
        assertEquals("testtest", repoData.get().getUsername());
        assertEquals(testUser.getPassword(), repoData.get().getPassword());
        assertEquals(testUser.isAdmin(), repoData.get().isAdmin());
    }

    // Fails
    @Test
    void updateFails_OnSelf_Admin() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testUser.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"isAdmin\":\"true\"}"))
                .andExpect(status().isForbidden());

        assertFalse(userRepository.findByEmail(testUser.getEmail()).get().isAdmin());
    }

    @Test
    void updateFails_OnOther() throws Exception {
        String jwt = jwtService.generateToken(testUser.getEmail());
        Cookie jwtCookie = new Cookie("jwt", jwt);

        mockMvc.perform(patch("/api/users/" + testAdmin.getId())
                .cookie(jwtCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"username\":\"newAdmin\"}"))
                .andExpect(status().isForbidden());

        assertEquals(userRepository.findByEmail(testAdmin.getEmail()).get().getUsername(), testAdmin.getUsername());
    }
}