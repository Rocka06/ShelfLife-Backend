package com.shelflife.project.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository repo;

    @GetMapping()
    public ResponseEntity<List<User>> getUsers(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));

        if (!isAdmin)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<User> user = repo.findById(id);

        if(!user.isPresent())
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid id"));

        return ResponseEntity.ok(user.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id, Authentication auth) {
        if(auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        
        if (!repo.existsById(id))
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid id"));

        String userEmail = auth.getName();
        User self = repo.findByEmail(userEmail).get();

        if(self.getId() == id)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can't delete yourself"));

        repo.deleteById(id);
        return ResponseEntity.ok(null);
    }
}
