package com.xenplan.app.service.impl;

import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.Role;
import com.xenplan.app.domain.exception.ConflictException;
import com.xenplan.app.domain.exception.ForbiddenException;
import com.xenplan.app.domain.exception.NotFoundException;
import com.xenplan.app.repository.UserRepository;
import com.xenplan.app.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(String firstName, String lastName, String email, String password) {
        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        // Validate password length
        if (password == null || password.length() < 8) {
            throw new ConflictException("Password must be at least 8 characters");
        }

        // Create new user with CLIENT role by default
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.CLIENT)
                .active(true)
                .registrationDate(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(UUID userId, User userData, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Business rule: Only ADMIN or self can update
        boolean isSelf = user.getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isSelf && !isAdmin) {
            throw new ForbiddenException("You can only update your own profile or be an ADMIN");
        }

        // Business rule: Non-ADMIN cannot change role
        if (!isAdmin && userData.getRole() != null && !userData.getRole().equals(user.getRole())) {
            throw new ForbiddenException("Only ADMIN can change user roles");
        }

        // Update fields
        if (userData.getFirstName() != null) {
            user.setFirstName(userData.getFirstName());
        }
        if (userData.getLastName() != null) {
            user.setLastName(userData.getLastName());
        }
        if (userData.getPhone() != null) {
            user.setPhone(userData.getPhone());
        }
        if (isAdmin && userData.getRole() != null) {
            user.setRole(userData.getRole());
        }
        if (isAdmin && userData.getActive() != null) {
            user.setActive(userData.getActive());
        }

        return userRepository.save(user);
    }

    @Override
    public void setUserActive(UUID userId, boolean active, User currentUser) {
        // Business rule: Only ADMIN can activate/deactivate users
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only ADMIN can activate/deactivate users");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setActive(active);
        userRepository.save(user);
    }

    @Override
    public void changeUserRole(UUID userId, Role newRole, User currentUser) {
        // Business rule: Only ADMIN can change user roles
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only ADMIN can change user roles");
        }

        // Business rule: Cannot change own role
        if (userId.equals(currentUser.getId())) {
            throw new ForbiddenException("You cannot change your own role");
        }

        // Business rule: Cannot change ADMIN role
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Cannot change role of an ADMIN user");
        }

        if (newRole == Role.ADMIN) {
            throw new ForbiddenException("Cannot assign ADMIN role through this interface");
        }

        user.setRole(newRole);
        userRepository.save(user);
    }
}

