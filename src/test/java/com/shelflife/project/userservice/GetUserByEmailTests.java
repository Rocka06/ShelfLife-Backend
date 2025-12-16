package com.shelflife.project.userservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class GetUserByEmailTests {
    @Mock
    UserRepository repo;

    @InjectMocks
    UserService service;

    @Mock
    Authentication authentication;

    @Test
    void noauth_throwsNotFound() {
        assertThrows(ItemNotFoundException.class, () -> {
            service.getUserByEmail("test@test.test");
        });
    }

    @Test
    void noauth_returnsUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.test");
        user.setUsername("test");

        when(repo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> {
            service.getUserByEmail(user.getEmail());
        });

        assertEquals(user.getUsername(), service.getUserByEmail(user.getEmail()).getUsername());
    }

    @Test
    void auth_returnsUserAsAuthenticated() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.test");
        user.setUsername("test");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(repo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> {
            service.getUserByEmail(user.getEmail(), authentication);
        });

        assertEquals(user.getUsername(), service.getUserByEmail(user.getEmail()).getUsername());
    }

    @Test
    void auth_throwsAccessDeniedAsAnonymous() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            service.getUserByEmail("test@test.test", authentication);
        });
    }
}
