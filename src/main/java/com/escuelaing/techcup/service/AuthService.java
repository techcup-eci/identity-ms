package com.escuelaing.techcup.service;

import com.escuelaing.techcup.dto.*;
import com.escuelaing.techcup.exception.BusinessException;
import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.repository.UserRepository;
import com.escuelaing.techcup.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService {

    @Value("${services.api-gateway.url}")
    private String apiGatewayUrl;

    @Value("${internal.secret}")
    private String internalSecret;

    @Autowired
    private WebClient.Builder webClientBuilder;

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

        String token = jwtUtil.generateToken(
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getRole().name());
        auditService.log("LOGIN", user.getEmail(), "Inicio de sesión exitoso", ipAddress);

        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setExpiresIn(jwtUtil.getExpirationTime());
        return response;
    }

    @Transactional
    public void logout(String userId, String ipAddress) {
        User user = userRepository.findById(Long.parseLong(userId))
                        .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
        auditService.log("LOGOUT", user.getEmail(), "Cierre de sesión", ipAddress);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        // 1. Llamar al user-service para crear el usuario completo
        UserServiceResponse userResponse = webClientBuilder.build()
                .post()
                .uri(apiGatewayUrl + "/api/users/register")
                .header("X-Internal-Secret", internalSecret)
                .bodyValue(new UserServiceRequest(request.getEmail(), request.getRole()))
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(body -> new BusinessException(body)))
                .bodyToMono(UserServiceResponse.class)
                .block();

        // 2. Verificar que el correo no esté ya registrado
        if (userRepository.existsByEmail(userResponse.getEmail())) {
            throw new BusinessException("El correo ya está registrado");
        }

        // 3. Guardar solo las credenciales en nuestra tabla
        User credentials = new User();
        credentials.setEmail(userResponse.getEmail());
        credentials.setPassword(passwordEncoder.encode(request.getPassword()));
        credentials.setRole(Role.valueOf(userResponse.getRol()));
        credentials.setActive(true);
        userRepository.save(credentials);

        // 4. Generar JWT
        String token = jwtUtil.generateToken(
                String.valueOf(credentials.getId()),
                userResponse.getEmail(),
                userResponse.getRol());

        // 5. Auditoría
        auditService.log("REGISTER", userResponse.getEmail(), "Registro exitoso", ipAddress);

        // 6. Devolver respuesta
        AuthResponse response = new AuthResponse();
        response.setId(userResponse.getId());
        response.setToken(token);
        response.setEmail(userResponse.getEmail());
        response.setRole(userResponse.getRol());
        response.setExpiresIn(jwtUtil.getExpirationTime());
        return response;
    }

    @Transactional
    public void changeRol(Long userId, String newRol, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (!user.getActive()) {
            throw new BusinessException("Usuario inactivo. No se puede cambiar el rol.");
        }

        Role rolAnterior = user.getRole();
        Role rol;

        try {
            rol = Role.valueOf(newRol.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Rol inválido: " + newRol +
                    ". Valores válidos: INVITED, PLAYER, CAPTAIN, ORGANIZER, REFEREE, ADMIN");
        }

        // El organizador no puede asignar ADMIN
        if (rol == Role.ADMIN) {
            throw new BusinessException(
                    "No se puede asignar el rol de administrador");
        }

        user.setRole(rol);
        userRepository.save(user);

        auditService.log("CAMBIO_ROL", user.getEmail(),
                "Rol cambiado de " + rolAnterior + " a " + rol, ipAddress);
    }

    @Transactional
    public AuthResponse refeshToken(String token, String ipAddress) {
        // Extraemos al usuario
        String userId = jwtUtil.extractUserIdIgnoringExpiration(token);

        // Busca al usuario
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // Válida que el usuario no sea inactivo
        if (!user.getActive()) {
            throw new BusinessException("Usuario inactivo.");
        }

        // Genera nuevo token con el rol actualizado de la BD
        String newToken = jwtUtil.generateToken(
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getRole().name());

        auditService.log("REFRESH_TOKEN", userId,
                "Token renovado con rol: " + user.getRole().name(), ipAddress);

        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setToken(newToken);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setExpiresIn(jwtUtil.getExpirationTime());
        return response;
    }
}