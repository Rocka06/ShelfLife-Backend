package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.ChangeUserDataRequest;
import com.shelflife.project.exception.EmailExistsException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.User;
import com.shelflife.project.service.JwtService;
import com.shelflife.project.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

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
    private JwtService jwtService;

    @Autowired
    private UserService service;

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
            @Valid @RequestBody ChangeUserDataRequest request, HttpServletRequest httpRequest,
            HttpServletResponse response) {

        try {
            long selfId = service.getUserByAuth(auth).get().getId();
            User updated = service.updateUser(id, request, auth);

            if (request.getEmail() != null && updated.getId() == selfId) {
                jwtService.invalidateToken((String) auth.getCredentials());

                final Cookie cookie = new Cookie("jwt", jwtService.generateToken(updated.getEmail()));
                cookie.setSecure(true);
                cookie.setHttpOnly(true);
                cookie.setMaxAge(24 * 60 * 60);
                cookie.setPath("/");

                response.addCookie(cookie);
            }

            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(e.getMessage(), "Invalid input"));
        } catch (EmailExistsException e) {
            return ResponseEntity.badRequest().body(Map.of("email", "This email is already used"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ItemNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
