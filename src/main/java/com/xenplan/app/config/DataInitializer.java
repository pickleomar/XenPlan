package com.xenplan.app.config;

import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enums.Role;
import com.xenplan.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin user already exists
            Optional<User> existingAdmin = userRepository.findByEmail("admin@xenplan.com");
            
            if (existingAdmin.isEmpty()) {
                // Create admin user with password "admin123"
                // Password hash: $2a$12$CSR90c8gT.RC7LuraSamCOxLNmMsvVq7mP.8P32UFOrHrql4sbfJ.
                User admin = User.builder()
                        .firstName("Admin")
                        .lastName("User")
                        .email("admin@xenplan.com")
                        .password("$2a$12$CSR90c8gT.RC7LuraSamCOxLNmMsvVq7mP.8P32UFOrHrql4sbfJ.") // bcrypt hash for "admin123"
                        .role(Role.ADMIN)
                        .active(true)
                        .registrationDate(LocalDateTime.now())
                        .build();
                
                userRepository.save(admin);
                System.out.println("Admin user created: admin@xenplan.com / admin123");
            } else {
                System.out.println("Admin user already exists: admin@xenplan.com");
            }
        };
    }
}

