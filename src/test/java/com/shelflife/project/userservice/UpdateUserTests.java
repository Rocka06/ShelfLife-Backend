package com.shelflife.project.userservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.shelflife.project.dto.ChangeUserDataRequest;
import com.shelflife.project.exception.EmailExistsException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UpdateUserTests {
    @Mock
    UserRepository repo;

    @Mock
    Authentication auth;

    @InjectMocks
    @Spy
    UserService service;

    @Test
    void throwsAccessDeniedAsAnonymous() {
        when(service.getUserByAuth(auth)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> service.updateUser(1L, emptyRequest(), auth));
        verifyNoInteractions(repo);
    }

    @Test
    void updateFailsOnOtherAsUser() {
        User current = testUser(1L, false);
        User userToUpdate = testUser(2L, false);

        when(service.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.findById(2L)).thenReturn(Optional.of(userToUpdate));

        assertThrows(AccessDeniedException.class, () -> service.updateUser(2L, emptyRequest(), auth));
        verify(repo, never()).save(any());
    }

    @Test
    void throwsEmailAlreadyExists() {
        User current = testUser(1L, false);
        ChangeUserDataRequest req = new ChangeUserDataRequest();
        req.setEmail("exists@test.test");

        when(service.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.findById(1L)).thenReturn(Optional.of(current));
        when(repo.existsByEmail("exists@test.test")).thenReturn(true);

        assertThrows(EmailExistsException.class, () -> service.updateUser(1L, req, auth));
        verify(repo, never()).save(any());
    }

    @Test
    void cannotSetIsAdminAsUser() {
        User current = testUser(1L, false);
        User userToUpdate = testUser(2L, false);

        ChangeUserDataRequest req = new ChangeUserDataRequest();
        req.setIsAdmin(true);

        when(service.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.findById(2L)).thenReturn(Optional.of(userToUpdate));

        assertThrows(AccessDeniedException.class, () -> service.updateUser(2L, req, auth));
        verify(repo, never()).save(any());
    }

    @Test
    void cannotChangeOwnIsAdmin() {
        User admin = testUser(1L, true);

        ChangeUserDataRequest req = new ChangeUserDataRequest();
        req.setIsAdmin(false);

        when(service.getUserByAuth(auth)).thenReturn(Optional.of(admin));
        when(repo.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(AccessDeniedException.class, () -> service.updateUser(1L, req, auth));
        verify(repo, never()).save(any());
    }

    @Test
    void adminCanChangeOtherIsAdmin() {
        User admin = testUser(1L, true);
        User userToUpdate = testUser(2L, false);

        ChangeUserDataRequest req = new ChangeUserDataRequest();
        req.setIsAdmin(true);

        when(service.getUserByAuth(auth)).thenReturn(Optional.of(admin));
        when(repo.findById(2L)).thenReturn(Optional.of(userToUpdate));

        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = assertDoesNotThrow(() -> service.updateUser(2L, req, auth));

        assertTrue(result.isAdmin());
        verify(repo).save(userToUpdate);
    }

    @Test
    void userCanUpdateOwnUsername() {
        User current = testUser(1L, false);
        ChangeUserDataRequest req = new ChangeUserDataRequest();
        req.setUsername("newname");

        when(service.getUserByAuth(auth)).thenReturn(Optional.of(current));
        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findById(1L)).thenReturn(Optional.of(current));

        User result = assertDoesNotThrow(() -> service.updateUser(1L, req, auth));

        assertEquals("newname", result.getUsername());
        verify(repo).save(current);
    }

    private User testUser(long id, boolean admin) {
        User u = new User();
        u.setId(id);
        u.setAdmin(admin);
        u.setEmail("test" + id + "@test.test");
        u.setUsername("test" + id);
        return u;
    }

    private ChangeUserDataRequest emptyRequest() {
        return new ChangeUserDataRequest();
    }
}
