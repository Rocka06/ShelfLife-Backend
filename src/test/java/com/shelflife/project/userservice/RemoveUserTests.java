package com.shelflife.project.userservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class RemoveUserTests {
    @Mock
    UserRepository repo;

    @Mock
    PasswordEncoder encoder;

    @Mock
    Authentication auth;

    @Spy
    @InjectMocks
    UserService service;

    @Test
    void throwsAccessDeniedAsAnonymous() {
        when(service.getUserByAuth(auth)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.removeUser(1, auth));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsAccessDeniedAsUser() {
        User user = new User();
        user.setAdmin(false);

        when(service.getUserByAuth(auth)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> service.removeUser(1, auth));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsAccessDeniedAsSameAdmin() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);

        when(service.getUserByAuth(auth)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> service.removeUser(1, auth));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsItemNotFound() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);

        when(service.getUserByAuth(auth)).thenReturn(Optional.of(user));

        assertThrows(ItemNotFoundException.class, () -> service.removeUser(2, auth));
        verify(repo, never()).deleteById(any());
    }

    @Test
    void successfulDelete() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);

        User userToDelete = new User();
        userToDelete.setId(2);

        when(service.getUserByAuth(auth)).thenReturn(Optional.of(user));
        when(repo.existsById(2L)).thenReturn(true);

        assertDoesNotThrow(() -> service.removeUser(2, auth));
        verify(repo).deleteById(2L);
    }
}
