package com.shelflife.project.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shelflife.project.dto.ChangePasswordRequest;
import com.shelflife.project.dto.LoginRequest;
import com.shelflife.project.dto.SignUpRequest;
import com.shelflife.project.exception.EmailExistsException;
import com.shelflife.project.exception.InvalidPasswordException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.PasswordsDontMatchException;
import com.shelflife.project.model.InvalidJwt;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.InvalidJwtRepository;
import com.shelflife.project.security.JwtService;
import com.shelflife.project.service.UserService;

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
    private UserService userService;

    @Autowired
    private InvalidJwtRepository jwtRepo;

    @Autowired
    private PasswordEncoder encoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response,
            Authentication auth) {

        if (auth != null && auth.isAuthenticated())
            return ResponseEntity.badRequest().body(Map.of("error", "Already logged in"));

        try {
            User user = userService.getUserByEmail(request.getEmail());
            if (!encoder.matches(request.getPassword(), user.getPassword()))
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
        } catch (ItemNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
        }

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
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.signUp(request, auth));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Already logged in"));
        } catch (EmailExistsException e) {
            return ResponseEntity.badRequest().body(Map.of("email", "Email already exists"));
        } catch (PasswordsDontMatchException e) {
            return ResponseEntity.badRequest().body(Map.of("passwordRepeat", "The passwords are not the same"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.badRequest().body(Map.of("error", "You are not logged in"));

        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        jwtRepo.deleteOlderThan(cutoff);

        InvalidJwt jwt = new InvalidJwt();
        jwt.setToken((String) auth.getCredentials());
        jwtRepo.save(jwt);

        return ResponseEntity.ok(null);
    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(Authentication auth, @Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request, auth);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (InvalidPasswordException e) {
            return ResponseEntity.badRequest().body(Map.of("oldPassword", "Invalid old password"));
        } catch (PasswordsDontMatchException e) {
            return ResponseEntity.badRequest().body(Map.of("newPasswordRepeat", "The passwords are not the same"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<User> getIsLoggedIn(HttpServletResponse response, Authentication auth) {
        Optional<User> self = userService.getUserByAuth(auth);

        if (!self.isPresent())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(self.get());
    }
}
