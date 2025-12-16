package com.shelflife.project.userservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.shelflife.project.dto.ChangePasswordRequest;
import com.shelflife.project.exception.InvalidPasswordException;
import com.shelflife.project.exception.PasswordsDontMatchException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class ChangePasswordTests {
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

        assertThrows(AccessDeniedException.class, () -> service.changePassword(validRequest(), auth));
        verifyNoInteractions(repo);
    }

    @Test
    void throwsInvalidPassword() {
        User user = new User();
        user.setPassword("encoded-old");

        when(service.getUserByAuth(auth))
                .thenReturn(Optional.of(user));
        when(encoder.matches("old", "encoded-old"))
                .thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> service.changePassword(validRequest(), auth));
        verify(repo, never()).save(any());
    }

    @Test
    void throwsPasswordsDontMatch() {
        User user = new User();
        user.setPassword("encoded-old");

        ChangePasswordRequest req = validRequest();
        req.setNewPasswordRepeat("different");

        when(service.getUserByAuth(auth))
                .thenReturn(Optional.of(user));
        when(encoder.matches("old", "encoded-old"))
                .thenReturn(true);

        assertThrows(PasswordsDontMatchException.class, () -> service.changePassword(req, auth));
        verify(repo, never()).save(any());
    }

    @Test
    void successfulPasswordChange() {
        User user = new User();
        user.setPassword("encoded-old");

        when(service.getUserByAuth(auth))
                .thenReturn(Optional.of(user));
        when(encoder.matches("old", "encoded-old"))
                .thenReturn(true);
        when(encoder.encode("new"))
                .thenReturn("encoded-new");

        service.changePassword(validRequest(), auth);

        assertEquals("encoded-new", user.getPassword());
        verify(repo).save(user);
    }

    private ChangePasswordRequest validRequest() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");
        req.setNewPasswordRepeat("new");
        return req;
    }

}
