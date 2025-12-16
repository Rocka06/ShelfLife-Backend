package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository repo;

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

    public List<User> getUsers(Authentication auth) throws AccessDeniedException {
        Optional<User> currentUser = getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        if (!currentUser.get().isAdmin())
            throw new AccessDeniedException(null);

        return repo.findAll();
    }

    public User getUserById(long id, Authentication auth) throws ItemNotFoundException, AccessDeniedException {
        Optional<User> currentUser = getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        Optional<User> user = repo.findById(id);

        if (!user.isPresent())
            throw new ItemNotFoundException();

        return user.get();
    }

    public User getUserByEmail(String email, Authentication auth) throws ItemNotFoundException, AccessDeniedException {
        Optional<User> currentUser = getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        Optional<User> user = repo.findByEmail(email);

        if (!user.isPresent())
            throw new ItemNotFoundException();

        return user.get();
    }

    public void removeUser(long id, Authentication auth) throws ItemNotFoundException, AccessDeniedException {
        Optional<User> currentUser = getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        if (!currentUser.get().isAdmin())
            throw new AccessDeniedException(null);

        if (currentUser.get().getId() == id)
            throw new AccessDeniedException(null);

        Optional<User> userToDelete = repo.findById(id);

        if (!userToDelete.isPresent())
            throw new ItemNotFoundException();

        repo.deleteById(id);
    }
}
