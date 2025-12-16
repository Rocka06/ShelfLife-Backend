package com.shelflife.project.userservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class GetUserByAuthTests {

    @Mock
    UserRepository repo;

    @InjectMocks
    UserService service;

    @Mock
    Authentication authentication;

    @Test
    void returnsEmptyWhenAuthIsNull() {
        Optional<User> result = service.getUserByAuth(null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(repo);
    }

    @Test
    void returnsEmptyWhenAuthIsNotAuthenticated() {
        when(authentication.isAuthenticated()).thenReturn(false);

        Optional<User> result = service.getUserByAuth(authentication);

        assertTrue(result.isEmpty());
        verifyNoInteractions(repo);
    }

    @Test
    void returnsUserWhenAuthenticated() {
        String email = "test@test.test";

        User user = new User();
        user.setEmail(email);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        when(repo.findByEmail(email))
                .thenReturn(Optional.of(user));

        Optional<User> result = service.getUserByAuth(authentication);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }
}
