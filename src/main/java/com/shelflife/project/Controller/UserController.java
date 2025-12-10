package com.shelflife.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;

import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository repo;

    @GetMapping()
    public List<User> getUsers() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable long id) {
        return repo.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable long id) {
        if (!repo.existsById(id))
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid id"));

        repo.deleteById(id);
        return ResponseEntity.ok(null);
    }
}
