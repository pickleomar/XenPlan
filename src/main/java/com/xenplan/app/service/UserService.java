package com.xenplan.app.service;

import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    
    /**
     * Register a new user (CLIENT role by default)
     */
    User registerUser(String firstName, String lastName, String email, String password);
    
    /**
     * Get user by ID
     */
    Optional<User> findById(UUID userId);
    
    /**
     * Get user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Get all users (ADMIN only)
     */
    List<User> findAll();
    
    /**
     * Update user (ADMIN only or self)
     */
    User updateUser(UUID userId, User userData, User currentUser);
    
    /**
     * Activate/deactivate user (ADMIN only)
     */
    void setUserActive(UUID userId, boolean active, User currentUser);
    
    /**
     * Change user role (ADMIN only)
     */
    void changeUserRole(UUID userId, Role newRole, User currentUser);
    
    /**
     * Change user password (self only)
     */
    void changePassword(UUID userId, String currentPassword, String newPassword, User currentUser);
}

