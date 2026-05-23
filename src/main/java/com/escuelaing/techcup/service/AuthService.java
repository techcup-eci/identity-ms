package com.escuelaing.techcup.service;

import com.escuelaing.techcup.client.UserServiceClient;
import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.LoginRequest;
import com.escuelaing.techcup.dto.RegisterRequest;
import com.escuelaing.techcup.exception.BusinessException;
import com.escuelaing.techcup.model.RefreshToken;
import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.model.UserStatus;
import com.escuelaing.techcup.repository.UserRepository;
import com.escuelaing.techcup.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditService auditService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // ── Login ────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessException("Usuario inactivo. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(),
                user.getRole().name(), user.getEmail());

        auditService.log("LOGIN", user.getEmail(), "Inicio de sesión exitoso", ipAddress);

        String rawRefreshToken = refreshTokenService.createRefreshToken(
                user.getId(), user.getUsersMsUserId());

        return buildAuthResponse(user.getId(), user.getEmail(), user.getRole().name(),
                user.getEmail(), token, rawRefreshToken);
    }

    // ── Logout ───────────────────────────────────────────────────────

    @Transactional
    public void logout(String email, String ipAddress) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            refreshTokenService.revokeAllUserTokens(user.getId());
        }
        auditService.log("LOGOUT", email, "Cierre de sesión", ipAddress);
    }

    // ── Refresh token ────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isEmpty()) {
            throw new BusinessException("No se encontró token de refresco");
        }

        RefreshTokenService.RotationResult rotationResult =
                refreshTokenService.rotateRefreshToken(rawRefreshToken);

        RefreshToken newToken = rotationResult.getEntity();
        String newRawToken = rotationResult.getRawToken();

        User user = userRepository.findById(newToken.getUserId())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessException("Usuario inactivo");
        }

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(),
                user.getRole().name(), user.getEmail());

        return buildAuthResponse(user.getId(), user.getEmail(), user.getRole().name(),
                user.getEmail(), accessToken, newRawToken);
    }

    // ── Validate token ───────────────────────────────────────────────

    public AuthResponse validate(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("Token no encontrado");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException("Token inválido o expirado");
        }

        String userId = jwtUtil.extractUserId(token);
        String email  = jwtUtil.extractEmail(token);
        String role   = jwtUtil.extractRole(token);
        String name   = jwtUtil.extractName(token);

        Long uid = null;
        try { uid = Long.parseLong(userId); } catch (NumberFormatException ignored) {}

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(uid, email, role, name);
        AuthResponse response = new AuthResponse();
        response.setUser(userInfo);
        return response;
    }

    // ── Register ─────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        // Solo INVITED y PLAYER pueden registrarse directamente
        // CAPTAIN lo asigna el sistema, ORGANIZER y ADMIN los asigna el ADMIN
        if (request.getRole() == null) {
            request.setRole(Role.USER);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe un usuario con ese correo");
        }

        // Call users-and-players-ms to create the full user profile
        UserServiceClient.CreateUserRequest userRequest = new UserServiceClient.CreateUserRequest();
        userRequest.setName(request.getFullName());
        userRequest.setEmail(request.getEmail());
        userRequest.setBirthDate(request.getBirthDate());
        userRequest.setRelationship(request.getRelationship());
        userRequest.setAcademicProgram(request.getProgram());
        userRequest.setSemester(request.getSemester());
        userRequest.setIdentificationType(request.getDocumentType());
        userRequest.setIdentificationNumber(request.getDocumentNumber());
        userRequest.setPhone(request.getPhone());
        userRequest.setPassword(request.getPassword());
        userRequest.setAcademicLevel(request.getAcademicLevel());
        userRequest.setProfessorType(request.getProfessorType());

        UserServiceClient.UserServiceResponse userResponse = userServiceClient.createUser(userRequest);

        // Save credentials in identity-ms
        User credentials = new User();
        credentials.setEmail(request.getEmail());
        credentials.setPassword(passwordEncoder.encode(request.getPassword()));
        credentials.setRole(request.getRole());
        credentials.setStatus(UserStatus.ACTIVE);
        credentials.setUsersMsUserId(userResponse.getId());
        User savedUser = userRepository.save(credentials);

        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(),
                savedUser.getRole().name(), request.getFullName());

        auditService.log("REGISTER", request.getEmail(), "Registro exitoso", ipAddress);

        String rawRefreshToken = refreshTokenService.createRefreshToken(
                savedUser.getId(), savedUser.getUsersMsUserId());

        return buildAuthResponse(savedUser.getId(), savedUser.getEmail(),
                savedUser.getRole().name(), request.getFullName(), token, rawRefreshToken);
    }

    // ── Update role ──────────────────────────────────────────────────

    @Transactional
    public User updateUserRole(Long userId, String newRole, Long requesterId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException("Solicitante no encontrado"));

        String requesterRole = requester.getRole().name();

        if (userId.equals(requesterId)) {
            boolean invitedToPlayer  = "INVITED".equals(user.getRole().name()) && "PLAYER".equals(newRole);
            boolean playerToCaptain  = "PLAYER".equals(user.getRole().name())  && "CAPTAIN".equals(newRole);
            if (!invitedToPlayer && !playerToCaptain) {
                throw new BusinessException("Auto-promoción no permitida: solo INVITED→PLAYER o PLAYER→CAPTAIN");
            }
        } else if (!"ADMIN".equals(requesterRole)) {
            throw new BusinessException("No tienes permisos para cambiar roles");
        }

        user.setRole(Role.valueOf(newRole));
        User savedUser = userRepository.save(user);

        // Sync role to users-and-players-ms (best-effort)
        if (user.getUsersMsUserId() != null) {
            try {
                userServiceClient.updateSystemRole(user.getUsersMsUserId(),
                        new UserServiceClient.UpdateSystemRoleRequest(newRole));
            } catch (Exception e) {
                log.warn("Failed to sync role to users-ms for user {}: {}", userId, e.getMessage());
            }
        }

        log.info("Role updated: userId={} newRole={} by requesterId={}", userId, newRole, requesterId);
        return savedUser;
    }

<<<<<<< HEAD
    public void changeRol(Long userId, Role newRol) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        user.setRole(newRol);
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse refeshToken(String token, String ipAddress) {
        // Extraemos al usuario
        String userId = jwtUtil.extractUserIdIgnoringExpiration(token);
=======
    // ── Update status (ADMIN only) ───────────────────────────────────
>>>>>>> 72213df73f487ff1148d79e23223c1293e2d2f97

    /**
     * Changes the status of a user between ACTIVE and INACTIVE.
     * Rules:
     *  - Only ADMIN can call this.
     *  - Cannot inactivate a user who is on a team enrolled in an ACTIVE or IN_PROGRESS tournament.
     *    That check is delegated to users-ms via Feign (best-effort: if users-ms is down, the
     *    operation is blocked to protect data integrity).
     */
    @Transactional
    public User updateUserStatus(Long userId, UserStatus newStatus, Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException("Solicitante no encontrado"));

        if (!"ADMIN".equals(requester.getRole().name())) {
            throw new BusinessException("Solo el administrador puede cambiar el estado de un usuario");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (newStatus == UserStatus.INACTIVE && user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessException("El usuario ya está inactivo");
        }

        if (newStatus == UserStatus.ACTIVE && user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessException("El usuario ya está activo");
        }

        // Block inactivation if user has active tournament enrollment
        if (newStatus == UserStatus.INACTIVE && user.getUsersMsUserId() != null) {
            try {
                Boolean hasEnrollment = userServiceClient.hasActiveEnrollment(user.getUsersMsUserId());
                if (Boolean.TRUE.equals(hasEnrollment)) {
                    throw new BusinessException(
                            "No se puede inactivar el usuario: está vinculado a un equipo en un torneo activo o en progreso");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Could not verify active enrollment for user {}: {}. Blocking inactivation to be safe.",
                        userId, e.getMessage());
                throw new BusinessException(
                        "No se pudo verificar el estado del torneo. Intente más tarde.");
            }
        }

        user.setStatus(newStatus);
        User savedUser = userRepository.save(user);

        auditService.log(
                "STATUS_CHANGE",
                user.getEmail(),
                "Estado cambiado a " + newStatus.name() + " por admin id=" + requesterId,
                null
        );

        log.info("Status updated: userId={} newStatus={} by adminId={}", userId, newStatus, requesterId);
        return savedUser;
    }

    // ── Helper ───────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(Long id, String email, String role,
                                           String name, String accessToken,
                                           String rawRefreshToken) {
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(id, email, role, name);
        return new AuthResponse(accessToken, jwtUtil.getExpirationTime(), userInfo, rawRefreshToken);
    }
}
