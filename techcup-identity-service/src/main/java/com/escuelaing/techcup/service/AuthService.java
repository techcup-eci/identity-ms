package com.escuelaing.techcup.service;

import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.LoginRequest;
import com.escuelaing.techcup.exception.BusinessException;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.repository.UserRepository;
import com.escuelaing.techcup.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditService auditService;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));

        if (!user.getActive()) {
            throw new BusinessException("Usuario inactivo. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        Long expiration = jwtUtil.getExpirationTime();

        auditService.log("LOGIN", user.getEmail(), "Inicio de sesión exitoso desde IP: " + ipAddress);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setExpiresIn(expiration);
        return response;
    }

    @Transactional
    public void logout(String email, String ipAddress) {
        auditService.log("LOGOUT", email, "Cierre de sesión desde IP: " + ipAddress);
    }
}
