package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.ChangePasswordRequest;
import com.shelflife.project.dto.ChangeUserDataRequest;
import com.shelflife.project.dto.SignUpRequest;
import com.shelflife.project.exception.EmailExistsException;
import com.shelflife.project.exception.InvalidPasswordException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.exception.PasswordsDontMatchException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.isAdmin() ? "admin" : "user")
                .build();
    }

    public Optional<User> getUserByAuth(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return Optional.empty();

        return repo.findByEmail(auth.getName());
    }

    public List<User> getUsers() {
        return repo.findAll();
    }

    public List<User> getUsers(Authentication auth) throws AccessDeniedException {
        Optional<User> currentUser = getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        if (!currentUser.get().isAdmin())
            throw new AccessDeniedException(null);

        return repo.findAll();
    }

    public User getUserById(long id) throws ItemNotFoundException {
        Optional<User> user = repo.findById(id);

        if (!user.isPresent())
            throw new ItemNotFoundException();

        return user.get();
    }

    public User getUserById(long id, Authentication auth) throws ItemNotFoundException, AccessDeniedException {
        Optional<User> currentUser = getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        return getUserById(id);
    }

    public User getUserByEmail(String email) throws ItemNotFoundException {
        Optional<User> user = repo.findByEmail(email);

        if (!user.isPresent())
            throw new ItemNotFoundException();

        return user.get();
    }

    public User getUserByEmail(String email, Authentication auth) throws ItemNotFoundException, AccessDeniedException {
        Optional<User> currentUser = getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        return getUserByEmail(email);
    }

    @Transactional
    public User signUp(@Valid SignUpRequest request, Authentication auth)
            throws AccessDeniedException, EmailExistsException, PasswordsDontMatchException {
        Optional<User> currentUser = getUserByAuth(auth);

        if (currentUser.isPresent())
            throw new AccessDeniedException(null);

        if (repo.existsByEmail(request.getEmail()))
            throw new EmailExistsException();

        if (!request.getPassword().equals(request.getPasswordRepeat()))
            throw new PasswordsDontMatchException();

        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setUsername(request.getUsername());
        newUser.setPassword(encoder.encode(request.getPassword()));
        newUser.setAdmin(false);

        return repo.save(newUser);
    }

    @Transactional
    public void changePassword(@Valid ChangePasswordRequest request, Authentication auth)
            throws AccessDeniedException, InvalidPasswordException, PasswordsDontMatchException {
        Optional<User> currentUser = getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        if (!encoder.matches(request.getOldPassword(), currentUser.get().getPassword()))
            throw new InvalidPasswordException();

        if (!request.getNewPassword().equals(request.getNewPasswordRepeat()))
            throw new PasswordsDontMatchException();

        currentUser.get().setPassword(encoder.encode(request.getNewPassword()));
        repo.save(currentUser.get());
    }

    @Transactional
    public User updateUser(long id, ChangeUserDataRequest request, Authentication auth)
            throws ItemNotFoundException, AccessDeniedException, EmailExistsException {

        Optional<User> currentUser = getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        User dbUser = getUserById(id);

        if (!currentUser.get().isAdmin() && currentUser.get().getId() != dbUser.getId())
            throw new AccessDeniedException(null);

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            dbUser.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (repo.existsByEmail(request.getEmail()))
                throw new EmailExistsException();

            dbUser.setEmail(request.getEmail());
        }

        if (request.getIsAdmin() != null) {
            // Cant set your own admin priviliges
            if (currentUser.get().getId() == dbUser.getId())
                throw new AccessDeniedException(null);

            if (!currentUser.get().isAdmin())
                throw new AccessDeniedException(null);

            dbUser.setAdmin(request.getIsAdmin());
        }

        return repo.save(dbUser);
    }

    @Transactional
    public void removeUser(long id, Authentication auth) throws ItemNotFoundException, AccessDeniedException {
        Optional<User> currentUser = getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        if (!currentUser.get().isAdmin())
            throw new AccessDeniedException(null);

        if (currentUser.get().getId() == id)
            throw new AccessDeniedException(null);

        if(!repo.existsById(id))
            throw new ItemNotFoundException();
        repo.deleteById(id);
    }
}
