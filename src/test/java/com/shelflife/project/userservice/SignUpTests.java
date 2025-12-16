package com.shelflife.project.userservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.shelflife.project.dto.SignUpRequest;
import com.shelflife.project.exception.EmailExistsException;
import com.shelflife.project.exception.PasswordsDontMatchException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class SignUpTests {

    @Mock
    UserRepository repo;

    @Mock
    PasswordEncoder encoder;

    @Mock
    Authentication auth;

    @InjectMocks
    @Spy
    UserService service;

    @Test
    void throwsAccessDeniedAsAuthenticated() {
        when(service.getUserByAuth(auth)).thenReturn(Optional.of(new User()));

        assertThrows(AccessDeniedException.class, () -> {
            service.signUp(validRequest(), auth);
        });

        verifyNoInteractions(repo);
    }

    @Test
    void throwsEmailExistsWhenEmailAlreadyUsed() {
        when(service.getUserByAuth(auth)).thenReturn(Optional.empty());
        when(repo.existsByEmail("test@test.test")).thenReturn(true);

        assertThrows(EmailExistsException.class, () -> {
            service.signUp(validRequest(), auth);
        });

        assertTrue(repo.existsByEmail("test@test.test"));
        verify(repo, never()).save(any());
    }

    @Test
    void throwsWhenPasswordsDoNotMatch() {
        SignUpRequest req = validRequest();
        req.setPasswordRepeat("12345");

        when(service.getUserByAuth(auth))
                .thenReturn(Optional.empty());
        when(repo.existsByEmail(req.getEmail()))
                .thenReturn(false);

        assertThrows(PasswordsDontMatchException.class, () -> {
            service.signUp(req, auth);
        });

        verify(repo, never()).save(any());
    }

    @Test
    void createsAndSavesNewUser() {
        SignUpRequest req = validRequest();

        when(service.getUserByAuth(auth))
                .thenReturn(Optional.empty());
        when(repo.existsByEmail(req.getEmail()))
                .thenReturn(false);
        when(encoder.encode("password"))
                .thenReturn("encoded-password");

        when(repo.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.signUp(req, auth);

        assertEquals("test@test.test", result.getEmail());
        assertEquals("test", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertFalse(result.isAdmin());

        verify(repo).save(any(User.class));
    }

    private SignUpRequest validRequest() {
        SignUpRequest req = new SignUpRequest();
        req.setEmail("test@test.test");
        req.setUsername("test");
        req.setPassword("password");
        req.setPasswordRepeat("password");
        return req;
    }
}
