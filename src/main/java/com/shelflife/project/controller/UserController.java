package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.ChangeUserDataRequest;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.service.UserService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository repo;

    @GetMapping()
    public ResponseEntity<List<User>> getUsers(Authentication auth) {
        try {
            List<User> users = service.getUsers(auth);
            return ResponseEntity.ok(users);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable long id, Authentication auth) {
        try {
            User user = service.getUserById(id, auth);
            return ResponseEntity.ok(user);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id, Authentication auth) {
        try {
            service.removeUser(id, auth);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUserData(@PathVariable long id, Authentication auth,
            @Valid @RequestBody ChangeUserDataRequest request) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        String userEmail = auth.getName();
        Optional<User> self = repo.findByEmail(userEmail);
        Optional<User> requestedUser = repo.findById(id);

        if (!self.isPresent())
            return ResponseEntity.notFound().build();

        if (!requestedUser.isPresent())
            return ResponseEntity.notFound().build();

        if (!self.get().isAdmin() && self.get().getId() != id)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (request.getEmail() != null) {
            if (request.getEmail().isBlank())
                return ResponseEntity.badRequest().body(Map.of("email", "Email cant be empty"));

            if (repo.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("email", "Email already exists"));
            }

            requestedUser.get().setEmail(request.getEmail());
            // TODO: Invalidate JWT and issue a new one
        }

        if (request.getUsername() != null) {
            if (request.getUsername().isBlank())
                return ResponseEntity.badRequest().body(Map.of("username", "Username cant be empty"));

            requestedUser.get().setUsername(request.getUsername());
        }

        if (request.getIsAdmin() != null) {
            if (!self.get().isAdmin())
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            if (id == self.get().getId())
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            requestedUser.get().setAdmin(request.getIsAdmin());
        }

        repo.save(requestedUser.get());
        return ResponseEntity.ok().build();
    }
}
