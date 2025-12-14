package com.xenplan.app.config;

import com.xenplan.app.domain.entity.User;
import com.xenplan.app.domain.enum.Role;
import com.xenplan.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        userRepository.save(
                User.builder()
                        .firstName("Admin")
                        .lastName("XenPlan")
                        .email("admin@xenplan.com")
                        .password(passwordEncoder.encode("Admin123!"))
                        .role(Role.ADMIN)
                        .active(true)
                        .registrationDate(LocalDateTime.now())
                        .build()
        );
    }
}
