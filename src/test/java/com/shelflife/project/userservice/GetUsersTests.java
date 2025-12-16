package com.shelflife.project.userservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.UserService;

@ExtendWith(MockitoExtension.class)
public class GetUsersTests {

    @Mock
    UserRepository repo;

    @InjectMocks
    UserService service;

    @Mock
    Authentication authentication;

    @Test
    void noauth_returnsEmptyList() {
        when(repo.findAll()).thenReturn(new ArrayList<>());

        assertEquals(0, service.getUsers().size());
    }

    @Test
    void noauth_returnsListWithUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.test");

        List<User> userList = new ArrayList<>();
        userList.add(user);
        when(repo.findAll()).thenReturn(userList);

        assertEquals(1, service.getUsers().size());
        assertEquals(user.getEmail(), service.getUsers().get(0).getEmail());
    }

    @Test
    void auth_returnsUsersAsAdmin() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.test");
        user.setAdmin(true);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(repo.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        List<User> userList = new ArrayList<>();
        userList.add(user);
        when(repo.findAll()).thenReturn(userList);

        assertDoesNotThrow(() -> {
            service.getUsers(authentication);
        });

        assertEquals(1, service.getUsers().size());
        assertEquals(user.getEmail(), service.getUsers().get(0).getEmail());
    }

    @Test
    void auth_throwsAccessDeniedAsUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.test");
        user.setAdmin(false);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(repo.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> {
            service.getUsers(authentication);
        });
    }

    @Test
    void auth_throwsAccessDeniedAsAnonymous() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.test");
        user.setAdmin(false);

        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            service.getUsers(authentication);
        });

        verifyNoInteractions(repo);
    }
}
