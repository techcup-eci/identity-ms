package com.escuelaing.techcup.service;

import com.escuelaing.techcup.dto.*;
import com.escuelaing.techcup.exception.BusinessException;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.repository.UserRepository;
import com.escuelaing.techcup.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditService auditService;

    @Transactional
    public void register(RegisterRequest request) {
        // Validar email único
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }

        // Validar que el rol sea PLAYER o INVITED (solo esos permitidos en registro)
        if (request.getRole() != Role.PLAYER && request.getRole() != Role.INVITED) {
            throw new BusinessException("Solo se permiten roles de PLAYER o INVITED en el registro");
        }

        // Validar correo institucional vs personal
        if (isInstitutionalEmail(request.getEmail())) {
            // Para institucional, debe tener relación estudiante, profesor, administrativo o graduado
            if (!isValidInstitutionalRelationship(request.getRelationship())) {
                throw new BusinessException("Para correo institucional, la relación debe ser: estudiante, profesor, administrativo o graduado");
            }
        } else {
            // Para correo personal, debe ser familiar
            if (!"FAMILY".equalsIgnoreCase(request.getRelationship())) {
                throw new BusinessException("Para correo personal, la relación debe ser: familiar");
            }
        }

        // Validar semestre solo si es estudiante
        if ("STUDENT".equalsIgnoreCase(request.getRelationship()) && request.getSemester() == null) {
            throw new BusinessException("Los estudiantes deben indicar su semestre");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRelationship(request.getRelationship().toUpperCase());
        user.setAcademicProgram(request.getAcademicProgram());
        user.setSemester(request.getSemester());
        user.setActive(true);
        user.setBirthDate(request.getBirthDate());
        user.setIdentificationType(request.getIdentificationType());
        user.setIdentificationNumber(request.getIdentificationNumber());
        user.setRole(request.getRole());

        userRepository.save(user);
        auditService.log("REGISTER", request.getEmail(), "Usuario registrado exitosamente");
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));

        if (!user.getActive()) {
            log.warn("Intento de login de usuario inactivo: {}", request.getEmail());
            throw new BusinessException("Usuario inactivo. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Contraseña incorrecta para el usuario: {}", request.getEmail());
            throw new BusinessException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        Long expiration = jwtUtil.getExpirationTime();

        log.info("Login exitoso: {} desde IP {}", user.getEmail(), ipAddress);
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
        log.info("Logout: {} desde IP {}", email, ipAddress);
        auditService.log("LOGOUT", email, "Cierre de sesión desde IP: " + ipAddress);
    }

    @Transactional
    public void updateRole(String adminEmail, UpdateRoleRequest request) {
        // Verificar que quien asigna es ADMIN
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new BusinessException("Administrador no encontrado"));

        if (admin.getRole() != Role.ADMIN) {
            throw new BusinessException("Solo un administrador puede asignar roles");
        }

        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        Role oldRole = targetUser.getRole();
        targetUser.setRole(request.getNewRole());
        userRepository.save(targetUser);

        auditService.log("UPDATE_ROLE", targetUser.getEmail(),
            "Rol cambiado de " + oldRole + " a " + request.getNewRole() + " por " + adminEmail);
    }

    @Transactional
    public void inactivateUser(String adminEmail, InactivateUserRequest request) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new BusinessException("Administrador no encontrado"));

        if (admin.getRole() != Role.ADMIN) {
            throw new BusinessException("Solo un administrador puede inactivar usuarios");
        }

        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // NOTA: Aquí faltaría validar que no esté vinculado a un equipo inscrito en torneo activo
        // Por ahora solo inactivamos, luego se conectará con Servicio de Equipos

        targetUser.setActive(false);
        userRepository.save(targetUser);

        auditService.log("INACTIVATE_USER", targetUser.getEmail(),
            "Usuario inactivado por " + adminEmail);
    }

    public UserResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        UserResponse response = new UserResponse();
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRelationship(user.getRelationship());
        response.setAcademicProgram(user.getAcademicProgram());
        response.setSemester(user.getSemester());
        response.setActive(user.getActive());
        response.setBirthDate(user.getBirthDate());
        response.setIdentificationType(user.getIdentificationType());
        response.setIdentificationNumber(user.getIdentificationNumber());
        response.setRole(user.getRole());
        return response;
    }

    private boolean isInstitutionalEmail(String email) {
        // Puedes configurar el dominio de la Escuela
        return email.endsWith("@escuelaing.edu.co");
    }

    private boolean isValidInstitutionalRelationship(String relationship) {
        return relationship.equalsIgnoreCase("STUDENT") ||
               relationship.equalsIgnoreCase("PROFESSOR") ||
               relationship.equalsIgnoreCase("ADMINISTRATIVE") ||
               relationship.equalsIgnoreCase("GRADUATE");
    }
}