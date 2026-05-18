package com.escuelaing.techcup.service;

import com.escuelaing.techcup.client.UserServiceClient;
import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.LoginRequest;
import com.escuelaing.techcup.dto.RegisterRequest;
import com.escuelaing.techcup.exception.BusinessException;
import com.escuelaing.techcup.model.RefreshToken;
import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.repository.UserRepository;
import com.escuelaing.techcup.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class AuthService {

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

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getEmail());

        auditService.log("LOGIN", user.getEmail(), "Inicio de sesión exitoso", ipAddress);

        String rawRefreshToken = refreshTokenService.createRefreshToken(
                user.getId(), user.getUsersMsUserId());

        return buildAuthResponse(user.getId(), user.getEmail(), user.getRole().name(),
                user.getEmail(), token, rawRefreshToken);
    }

    @Transactional
    public void logout(String email, String ipAddress) {
        // Find user to get their ID for refresh token revocation
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user != null) {
            refreshTokenService.revokeAllUserTokens(user.getId());
        }
        auditService.log("LOGOUT", email, "Cierre de sesión", ipAddress);
    }

    /**
     * Refreshes an expired access token using a valid refresh token from the httpOnly cookie.
     * Implements token rotation: old refresh token is revoked and a new one is created.
     *
     * @param rawRefreshToken the raw refresh token from the cookie
     * @return AuthResponse with new access token, user info, and new refresh token
     * @throws BusinessException if token is missing, invalid, revoked, or expired
     */
    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isEmpty()) {
            throw new BusinessException("No se encontró token de refresco");
        }

        // Rotate the refresh token (validates, revokes old, creates new)
        RefreshTokenService.RotationResult rotationResult =
                refreshTokenService.rotateRefreshToken(rawRefreshToken);

        RefreshToken newToken = rotationResult.getEntity();
        String newRawToken = rotationResult.getRawToken();

        // Load the user
        User user = userRepository.findById(newToken.getUserId())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (!user.getActive()) {
            throw new BusinessException("Usuario inactivo");
        }

        // Generate new access token
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(),
                user.getRole().name(), user.getEmail());

        return buildAuthResponse(user.getId(), user.getEmail(), user.getRole().name(),
                user.getEmail(), accessToken, newRawToken);
    }

    /**
     * Validates the current access token and returns user info.
     * Used by frontend on app load to verify stored token is still valid.
     *
     * @param request the HTTP request containing the Authorization header
     * @return AuthResponse with user info (no tokens)
     */
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
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);
        String name = jwtUtil.extractName(token);

        Long uid;
        try {
            uid = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            uid = null;
        }

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(uid, email, role, name);
        AuthResponse response = new AuthResponse();
        response.setUser(userInfo);
        return response;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        // 1. Check if email already exists in identity-ms
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe un usuario con ese correo");
        }

        // 2. Build request for users-and-players-ms
        UserServiceClient.CreateUserRequest userRequest = new UserServiceClient.CreateUserRequest();
        userRequest.setName(request.getFullName());
        userRequest.setEmail(request.getEmail());
        userRequest.setBirthDate(request.getBirthDate() != null ? request.getBirthDate().toString() : null);
        userRequest.setSystemRole(request.getRole().name());
        userRequest.setRelationship(request.getRelationship());
        userRequest.setAcademicProgram(request.getProgram());
        userRequest.setSemester(request.getSemester());
        userRequest.setIdentificationType(request.getDocumentType());
        userRequest.setIdentificationNumber(request.getDocumentNumber());
        userRequest.setPhone(0L);

        // 3. Call users-and-players-ms via OpenFeign
        UserServiceClient.UserServiceResponse userResponse = userServiceClient.createUser(userRequest);

        // 4. Save credentials in identity-ms
        User credentials = new User();
        credentials.setEmail(request.getEmail());
        credentials.setPassword(passwordEncoder.encode(request.getPassword()));
        credentials.setRole(request.getRole());
        credentials.setActive(true);
        credentials.setUsersMsUserId(userResponse.getId());
        User savedUser = userRepository.save(credentials);

        // 5. Generate JWT with userId, email, role, name
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(),
                savedUser.getRole().name(), request.getFullName());

        // 6. Audit
        auditService.log("REGISTER", request.getEmail(), "Registro exitoso", ipAddress);

        // 7. Generate and persist refresh token
        String rawRefreshToken = refreshTokenService.createRefreshToken(
                savedUser.getId(), savedUser.getUsersMsUserId());

        // 8. Return response
        return buildAuthResponse(savedUser.getId(), savedUser.getEmail(),
                savedUser.getRole().name(), request.getFullName(), token, rawRefreshToken);
    }

    /**
     * Changes a user's system role.
     * Self-promotion rules:
     *   - INVITED → PLAYER: allowed when creating athletic profile (BecomePlayer flow)
     *   - PLAYER → CAPTAIN: allowed when creating a team
     * ADMIN and ORGANIZER can change any user to any role.
     * Also syncs the role change to users-and-players-ms via UserServiceClient.
     */
    @Transactional
    public User updateUserRole(Long userId, String newRole, Long requesterId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException("Solicitante no encontrado"));

        String requesterRole = requester.getRole().name();

        // Self-promotion rules
        if (userId.equals(requesterId)) {
            boolean invitedToPlayer = "INVITED".equals(user.getRole().name()) && "PLAYER".equals(newRole);
            boolean playerToCaptain = "PLAYER".equals(user.getRole().name()) && "CAPTAIN".equals(newRole);
            if (!invitedToPlayer && !playerToCaptain) {
                throw new BusinessException("Auto-promoción no permitida: solo INVITED→PLAYER o PLAYER→CAPTAIN");
            }
        }
        // ADMIN or ORGANIZER can change anyone
        else if (!"ADMIN".equals(requesterRole) && !"ORGANIZER".equals(requesterRole)) {
            throw new BusinessException("No tienes permisos para cambiar roles");
        }

        user.setRole(Role.valueOf(newRole));
        User savedUser = userRepository.save(user);

        // Sync role to users-and-players-ms (best-effort, don't fail if unavailable)
        if (user.getUsersMsUserId() != null) {
            try {
                UserServiceClient.UpdateSystemRoleRequest req = new UserServiceClient.UpdateSystemRoleRequest();
                req.setSystemRole(newRole);
                userServiceClient.updateSystemRole(user.getUsersMsUserId(), req);
            } catch (Exception e) {
                System.err.println("Warning: Failed to sync role to users-ms for user " + userId + ": " + e.getMessage());
            }
        }

        return savedUser;
    }

    // ── Helper ────────────────────────────────────────────────────────────

    /**
     * Builds an AuthResponse in the format the frontend expects:
     * {@code { accessToken, expiresIn, user: { id, email, role, name } }}.
     * The refresh token is stored server-side for the httpOnly cookie.
     */
    private AuthResponse buildAuthResponse(Long id, String email, String role,
                                           String name, String accessToken,
                                           String rawRefreshToken) {
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(id, email, role, name);
        return new AuthResponse(accessToken, jwtUtil.getExpirationTime(), userInfo, rawRefreshToken);
    }
}
