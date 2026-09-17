package com.booking.resource_booking_system.config;

import com.booking.resource_booking_system.entity.Role;
import com.booking.resource_booking_system.entity.User;
import com.booking.resource_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.password}")
    private String adminPassword;

    @Value("${seed.user.password}")
    private String userPassword;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByUsername("admin")) {

            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("user")) {

            User user = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode(userPassword))
                    .role(Role.USER)
                    .build();

            userRepository.save(user);
        }
    }
}