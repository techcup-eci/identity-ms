package com.escuelaing.techcup.config;

import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@techcup.com")) {
            User admin = new User();
            admin.setEmail("admin@techcup.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("Admin creado correctamente");
        }
    }
}