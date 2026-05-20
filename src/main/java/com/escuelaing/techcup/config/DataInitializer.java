package com.escuelaing.techcup.config;

import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.default.password}")
    private String adminDefaultPassword;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@techcup.com")) {
            User admin = new User();
            admin.setEmail("admin@techcup.com");
            admin.setPassword(passwordEncoder.encode(adminDefaultPassword));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            log.info("Admin creado correctamente");
        } else {
            log.info("Usuario administrador ya existe, omitiendo creación");
        }
    }
}