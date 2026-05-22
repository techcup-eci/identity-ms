package com.escuelaing.techcup.controller;

import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.LoginRequest;
import com.escuelaing.techcup.dto.RegisterRequest;
import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.service.AuthService;
import com.escuelaing.techcup.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/identity")
@Tag(name = "Authentication", description = "Identity and access management — login, registration, refresh, validation, and logout")
public class IdentityController {

        private static final Logger logger = LoggerFactory.getLogger(IdentityController.class);

    @Autowired
    private AuthService authService;

    // ───────────────────────────────────────────────────────────────────
    // POST /api/identity/login
    // ───────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Authenticate user",
            description = "Validates email and password credentials. On success returns an access token (JWT) "
                    + "in the response body and sets a refresh token as an httpOnly cookie. "
                    + "The access token must be sent as a Bearer token on subsequent requests."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "User account is inactive",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        AuthResponse authResponse = authService.login(request, httpRequest.getRemoteAddr());

        // Set refresh token as httpOnly cookie
        if (authResponse.getRefreshToken() != null) {
            ResponseCookie cookie = CookieUtil.buildRefreshTokenCookie(authResponse.getRefreshToken());
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.ok(authResponse);
    }

    // ───────────────────────────────────────────────────────────────────
    // POST /api/identity/register
    // ───────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account in both the identity service and the users-and-players service. "
                    + "Requires full profile information including name, relationship to the university, academic program, "
                    + "document details, and birth date. On success returns an access token and sets a refresh token cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registration successful — user created",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Validation error on request body",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        //request.setRole(Role.ADMIN); // Force role to ADMIN on registration
        AuthResponse authResponse = authService.register(request, httpRequest.getRemoteAddr());

        // Set refresh token as httpOnly cookie
        if (authResponse.getRefreshToken() != null) {
            ResponseCookie cookie = CookieUtil.buildRefreshTokenCookie(authResponse.getRefreshToken());
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    // ───────────────────────────────────────────────────────────────────
    // POST /api/identity/refresh
    // ───────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Refresh access token",
            description = "Issues a new access token using the refresh token stored in the httpOnly cookie. "
                    + "Implements token rotation: the old refresh token is revoked and a new one is issued. "
                    + "No Authorization header is required — the endpoint reads the 'refresh_token' cookie. "
                    + "If the refresh token is missing, expired, or revoked, returns 401."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New access token and refresh token issued",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token missing, invalid, expired, or revoked",
                    content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse httpResponse) {

        AuthResponse authResponse = authService.refresh(refreshToken);

        // Set new refresh token cookie
        if (authResponse.getRefreshToken() != null) {
            ResponseCookie cookie = CookieUtil.buildRefreshTokenCookie(authResponse.getRefreshToken());
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.ok(authResponse);
    }

    // ───────────────────────────────────────────────────────────────────
    // GET /api/identity/validate
    // ───────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Validate access token",
            description = "Checks that the provided Bearer token (in Authorization header) is valid and not expired. "
                    + "Returns the authenticated user's profile information. This endpoint is protected — "
                    + "a valid access token is required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid — returns user info",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token is missing, invalid, or expired",
                    content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validate(HttpServletRequest request) {
        AuthResponse authResponse = authService.validate(request);
        return ResponseEntity.ok(authResponse);
    }

    // ───────────────────────────────────────────────────────────────────
    // POST /api/identity/logout
    // ───────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Logout and revoke tokens",
            description = "Revokes all refresh tokens for the authenticated user and clears the refresh token cookie. "
                    + "The access token remains valid until it expires naturally (15 minutes). "
                    + "After logout, previous refresh tokens will no longer work."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout successful — tokens revoked, cookie cleared"),
            @ApiResponse(responseCode = "401", description = "Not authenticated — no valid access token",
                    content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse httpResponse) {

        authService.logout(userDetails.getUsername(), request.getRemoteAddr());

        // Clear the refresh token cookie
        ResponseCookie clearCookie = CookieUtil.buildClearRefreshTokenCookie();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());

        return ResponseEntity.noContent().build();
    }

    // ───────────────────────────────────────────────────────────────────
    // GET /api/identity/users
    // ───────────────────────────────────────────────────────────────────

    @Operation(
            summary = "List all users (admin only)",
            description = "Returns a list of all users with their roles. Protected — requires ADMIN role."
    )
    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> listAllUsers() {
        List<User> users = authService.findAllUsers();
        List<UserSummary> summaries = users.stream()
                .map(u -> new UserSummary(u.getId(), u.getEmail(), u.getRole().name(), u.getActive()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    public record UserSummary(Long id, String email, String role, Boolean active) {}

    // ───────────────────────────────────────────────────────────────────
    // PUT /api/identity/users/{id}/role
    // ───────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Update user role",
            description = "Change a user's system role. Self-promotion from PLAYER to CAPTAIN is allowed "
                    + "(auto-upgrade when creating a team). ADMIN and ORGANIZER can change any role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role transition"),
            @ApiResponse(responseCode = "403", description = "Not authorized to change roles"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String newRole = body.get("role");
        if (newRole == null || newRole.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'role' es requerido"));
        }
        // Get requester ID from the authenticated principal
        String requesterEmail = userDetails.getUsername();
        com.escuelaing.techcup.model.User requester = authService.findByEmail(requesterEmail);
        if (requester == null) {
            return ResponseEntity.status(403).body(Map.of("error", "No autorizado"));
        }
        
        com.escuelaing.techcup.model.User updated = authService.updateUserRole(id, newRole, requester.getId());
        System.out.println("Request to update user " + id + " role to " + newRole + " by " + userDetails.getUsername());

        return ResponseEntity.ok(Map.of(
                "id", updated.getId(),
                "email", updated.getEmail(),
                "role", updated.getRole().name()
        ));
    }
}
