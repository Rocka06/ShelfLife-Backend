package com.shelflife.project.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.LoginRequest;
import com.shelflife.project.dto.SignUpRequest;
import com.shelflife.project.model.InvalidJwt;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.InvalidJwtRepository;
import com.shelflife.project.repository.UserRepository;
import com.shelflife.project.security.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository repo;

    @Autowired
    private InvalidJwtRepository jwtRepo;

    @Autowired
    private PasswordEncoder encoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response,
            Authentication auth) {

        if (auth != null && auth.isAuthenticated())
            return ResponseEntity.badRequest().body(Map.of("error", "Already logged in"));

        Optional<User> user = repo.findByEmail(request.getEmail());

        if (!user.isPresent())
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));

        if (!encoder.matches(request.getPassword(), user.get().getPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));

        String token = jwtService.generateToken(request.getEmail());

        final Cookie cookie = new Cookie("jwt", token);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest request, HttpServletResponse response,
            Authentication auth) {
        if (auth != null && auth.isAuthenticated())
            return ResponseEntity.badRequest().body(Map.of("error", "Already logged in"));

        Optional<User> user = repo.findByEmail(request.getEmail());

        if (user.isPresent())
            return ResponseEntity.badRequest().body(Map.of("email", "Email already exists"));

        if (!request.getPasswordRepeat().equals(request.getPassword()))
            return ResponseEntity.badRequest().body(Map.of("passwordRepeat", "The passwords are not the same"));

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setUsername(request.getUsername());
        newUser.setPassword(encoder.encode(request.getPassword()));
        newUser.setAdmin(false);

        repo.save(newUser);

        return ResponseEntity.ok().body(repo.findByEmail(request.getEmail()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.badRequest().body(null);

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        jwtRepo.deleteOlderThan(cutoff);

        InvalidJwt jwt = new InvalidJwt();
        jwt.setToken((String)auth.getCredentials());
        jwtRepo.save(jwt);

        return ResponseEntity.ok(null);
    }

    @GetMapping("/me")
    public boolean getIsLoggedIn(HttpServletResponse response, Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    @GetMapping("/test")
    public String getMethodName() {
        return "logged in";
    }

}
