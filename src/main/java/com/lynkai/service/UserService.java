package com.lynkai.service;

import com.lynkai.dto.UpdateUserRequest;
import com.lynkai.model.User;
import com.lynkai.repository.UserRepository;
import com.lynkai.security.HashEncoder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final HashEncoder hashEncoder;

    public UserService(UserRepository userRepository, HashEncoder hashEncoder) {
        this.userRepository = userRepository;
        this.hashEncoder = hashEncoder;
    }


    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User getByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials."));
    }


    /**
     * Get current authenticated user's ID.
     */
    public Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Long.parseLong(userDetails.getUsername());
        }
        throw new IllegalStateException("No authenticated user");
    }

    /**
     * Fetch user by ID.
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Fetch user by username.
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    /**
     * Update username/email.
     */
    public User updateUserProfile(Long userId, UpdateUserRequest request) {
        User user = getUserById(userId);

        String newUsername = request.getUsername();
        String newEmail = request.getEmail();

        if (newUsername != null && !newUsername.isBlank() && !newUsername.equals(user.getUsername())) {
            if (userRepository.existsByUsername(newUsername)) {
                throw new IllegalArgumentException("Username already taken.");
            }
            user.setUsername(newUsername);
        }

        if (newEmail != null && !newEmail.isBlank() && !newEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already in use.");
            }
            user.setEmail(newEmail);
        }

        return userRepository.save(user);
    }

    /**
     * Change password securely.
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        if (!hashEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPasswordHash(hashEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Delete user account.
     */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Cannot delete — user not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }


}
