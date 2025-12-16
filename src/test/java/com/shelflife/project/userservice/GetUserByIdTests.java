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
public class GetUserByIdTests {
    @Mock
    UserRepository repo;

    @InjectMocks
    UserService service;

    @Mock
    Authentication authentication;

    @Test
    void noauth_throwsNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> {
            service.getUserById(1);
        });
    }

    @Test
    void noauth_returnsUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.test");

        when(repo.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> {
            service.getUserById(1);
        });

        assertEquals(user.getEmail(), service.getUserById(1).getEmail());
    }

    @Test
    void auth_returnsUserAsAuthenticated() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.test");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(repo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(repo.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> {
            service.getUserById(1, authentication);
        });

        assertEquals(user.getEmail(), service.getUserById(1).getEmail());
    }

    @Test
    void auth_throwsAccessDeniedAsAnonymous() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            service.getUserById(1, authentication);
        });
    }
}
