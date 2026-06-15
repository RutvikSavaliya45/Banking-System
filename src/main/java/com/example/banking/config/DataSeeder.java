package com.example.banking.config;

import com.example.banking.entity.Role;
import com.example.banking.entity.User;
import com.example.banking.repository.UserRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.existsByEmail("admin@bank.local")) {
                return;
            }
            User admin = new User();
            admin.setFullName("Bank Admin");
            admin.setEmail("admin@bank.local");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(Role.ROLE_ADMIN));
            userRepository.save(admin);
        };
    }
}
