package com.thisispmb.bushraat.service;

import com.thisispmb.bushraat.model.User;
import com.thisispmb.bushraat.repository.UserRepository;
import com.thisispmb.bushraat.security.Role;
import com.thisispmb.bushraat.util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public void register(String name, String email, String password) throws SQLException {
        if (name == null || name.isBlank() || name.trim().length() > 100) {
            throw new IllegalArgumentException("Name must be between 1 and 100 characters.");
        }

        if (email == null || email.isBlank() || !email.contains("@") || email.trim().length() > 255) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("Password must be between 8 and 72 characters.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        User existingUser = userRepository.findByEmail(normalizedEmail);

        if (existingUser != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();

        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setRole(Role.USER.name());

        userRepository.save(user);
    }

    public User authenticate(String email, String password) throws SQLException {
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail);

        if (user == null) {
            return null;
        }

        boolean passwordMatches =
                PasswordUtil.matches(password, user.getPasswordHash());

        if (!passwordMatches) {
            return null;
        }

        return user;
    }

    public User updateProfile(
            Long userId,
            String name,
            String email,
            String newPassword
    ) throws SQLException {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.emailExistsForAnotherUser(normalizedEmail, userId)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        if (newPassword != null && !newPassword.isBlank()
                && newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters.");
        }

        userRepository.updateDetails(userId, name.trim(), normalizedEmail);

        if (newPassword != null && !newPassword.isBlank()) {
            userRepository.updatePassword(userId, PasswordUtil.hash(newPassword));
        }

        return userRepository.findById(userId);
    }

}
