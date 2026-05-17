package com.escuelaing.techcup.service;

import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.LoginRequest;
import com.escuelaing.techcup.dto.RegisterRequest;
import com.escuelaing.techcup.dto.UserServiceResponse;
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

    //Secreto entre gateway y microservicio
    @Value("${internal.secret}")
    private String internalSecret;

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
    public void logout(String email, String ipAddress) {
        auditService.log("LOGOUT", email, "Cierre de sesión", ipAddress);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        // 1. Llamar al user-service para crear el usuario completo
        UserServiceResponse userResponse = webClientBuilder.build()
                .post()
                .uri(apiGatewayUrl + "/api/users/register")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .map(body -> new BusinessException(body)))
                .bodyToMono(UserServiceResponse.class)
                .block();

        // 2. Guardar solo las credenciales en nuestra tabla
        User credentials = new User();
        credentials.setEmail(userResponse.getEmail());
        credentials.setPassword(passwordEncoder.encode(request.getPassword()));
        credentials.setRole(Role.valueOf(userResponse.getRol()));
        credentials.setActive(true);
        userRepository.save(credentials);

        // 3. Generar JWT
        String token = jwtUtil.generateToken(userResponse.getEmail(), userResponse.getRol());

        // 4. Auditoría
        auditService.log("REGISTER", userResponse.getEmail(), "Registro exitoso", ipAddress);

        // 5. Devolver respuesta
        AuthResponse response = new AuthResponse();
        response.setId(userResponse.getId());
        response.setToken(token);
        response.setEmail(userResponse.getEmail());
        response.setRole(userResponse.getRol());
        response.setExpiresIn(jwtUtil.getExpirationTime());
        return response;
    }
}
