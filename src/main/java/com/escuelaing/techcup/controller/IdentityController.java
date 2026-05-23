package com.escuelaing.techcup.controller;

import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.LoginRequest;
import com.escuelaing.techcup.dto.RegisterRequest;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.model.UserStatus;
import com.escuelaing.techcup.service.AuthService;
import com.escuelaing.techcup.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/identity")
@Tag(name = "Authentication", description = "Identity and access management — login, registration, refresh, validation, and logout")
public class IdentityController {

    private static final Logger log = LoggerFactory.getLogger(IdentityController.class);

    @Autowired
    private AuthService authService;

    // ── POST /api/identity/login ─────────────────────────────────────

    @Operation(summary = "Authenticate user",
            description = "Validates credentials. Returns access token in body and sets refresh token as httpOnly cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthResponse authResponse = authService.login(request, httpRequest.getRemoteAddr());

        if (authResponse.getRefreshToken() != null) {
            ResponseCookie cookie = CookieUtil.buildRefreshTokenCookie(authResponse.getRefreshToken());
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.ok(authResponse);
    }

    // ── POST /api/identity/register ──────────────────────────────────

    @Operation(summary = "Register a new user",
            description = "Creates a user in both identity-ms and users-and-players-ms. Returns access token and sets refresh token cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation error", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // Role comes from the request body — no forced override
        AuthResponse authResponse = authService.register(request, httpRequest.getRemoteAddr());

        if (authResponse.getRefreshToken() != null) {
            ResponseCookie cookie = CookieUtil.buildRefreshTokenCookie(authResponse.getRefreshToken());
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    // ── POST /api/identity/refresh ───────────────────────────────────

    @Operation(summary = "Refresh access token",
            description = "Issues a new access token from the refresh token cookie. Implements token rotation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New tokens issued",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token missing, expired, or revoked", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse httpResponse) {

        AuthResponse authResponse = authService.refresh(refreshToken);

        if (authResponse.getRefreshToken() != null) {
            ResponseCookie cookie = CookieUtil.buildRefreshTokenCookie(authResponse.getRefreshToken());
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.ok(authResponse);
    }

    // ── GET /api/identity/validate ───────────────────────────────────

    @Operation(summary = "Validate access token",
            description = "Checks the Bearer token and returns the user's profile info.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token valid",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token missing, invalid, or expired", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validate(HttpServletRequest request) {
        return ResponseEntity.ok(authService.validate(request));
    }

    // -- GET /api/identity/me -----------------------------------

    @Operation(summary = "Get current user",
            description = "Checks the Bearer token and returns the current user's profile info.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token valid",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token missing, invalid, or expired", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(HttpServletRequest request) {
        return ResponseEntity.ok(authService.validate(request));
    }

    // ── POST /api/identity/logout ────────────────────────────────────

    @Operation(summary = "Logout and revoke tokens",
            description = "Revokes all refresh tokens for the user and clears the cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse httpResponse) {

        authService.logout(userDetails.getUsername(), request.getRemoteAddr());

        ResponseCookie clearCookie = CookieUtil.buildClearRefreshTokenCookie();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.noContent().build();
    }

    // ── GET /api/identity/users ──────────────────────────────────────

    @Operation(summary = "List all users (ADMIN only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> listAllUsers() {
        List<UserSummary> summaries = authService.findAllUsers().stream()
                .map(u -> new UserSummary(u.getId(), u.getEmail(),
                        u.getRole().name(), u.getStatus().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    public record UserSummary(Long id, String email, String role, String status) {}

    // ── PUT /api/identity/users/{id}/role ────────────────────────────

    @Operation(summary = "Update user role",
            description = "Self-promotion: INVITED→PLAYER or PLAYER→CAPTAIN. ADMIN/ORGANIZER can change any role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated"),
            @ApiResponse(responseCode = "400", description = "Invalid role transition"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String newRole = body.get("role");
        if (newRole == null || newRole.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'role' es requerido"));
        }

        User requester = authService.findByEmail(userDetails.getUsername());
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No autorizado"));
        }

        User updated = authService.updateUserRole(id, newRole, requester.getId());
        return ResponseEntity.ok(Map.of(
                "id",    updated.getId(),
                "email", updated.getEmail(),
                "role",  updated.getRole().name()
        ));
    }

    // ── PATCH /api/identity/users/{id}/status ────────────────────────

    @Operation(summary = "Change user status (ADMIN only)",
            description = "Activates or inactivates a user. Cannot inactivate a user enrolled in an active tournament.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "User already has that status or has active enrollment"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN can change status"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'status' es requerido"));
        }

        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Estado inválido. Valores permitidos: ACTIVE, INACTIVE"));
        }

        User requester = authService.findByEmail(userDetails.getUsername());
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No autorizado"));
        }

        User updated = authService.updateUserStatus(id, newStatus, requester.getId());
        return ResponseEntity.ok(Map.of(
                "id",     updated.getId(),
                "email",  updated.getEmail(),
                "status", updated.getStatus().name()
        ));
    }
}
