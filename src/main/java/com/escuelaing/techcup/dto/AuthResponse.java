package com.escuelaing.techcup.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Authentication response sent to the frontend.
 *
 * <p>The JSON shape is deliberately nested so the frontend receives:
 * <pre>{@code
 * {
 *   "accessToken": "eyJ...",
 *   "expiresIn": 900,
 *   "user": {
 *     "id": 1,
 *     "email": "admin@techcup.com",
 *     "role": "ADMIN",
 *     "name": "Juan Pérez"
 *   }
 * }
 * }</pre>
 *
 * <p>The {@code refreshToken} field is excluded from the JSON body
 * ({@code @JsonIgnore}) — it is only sent via httpOnly cookie.
 */
public class AuthResponse {

    @Schema(description = "JWT access token for Bearer authentication", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Token expiration time in seconds", example = "900")
    private Long expiresIn;

    @Schema(description = "Authenticated user information")
    private UserInfo user;

    @JsonIgnore
    @Schema(hidden = true)
    private String refreshToken;

    // ── Nested user info ──────────────────────────────────────────────

    @Schema(description = "User identity and profile summary")
    public static class UserInfo {

        @Schema(description = "User ID in the identity service", example = "1")
        private Long id;

        @Schema(description = "User email address", example = "admin@techcup.com")
        private String email;

        @Schema(description = "System role of the user",
                example = "ADMIN",
                allowableValues = {"INVITED", "PLAYER", "CAPTAIN", "ORGANIZER", "REFEREE", "ADMIN"})
        private String role;

        @Schema(description = "User's full display name", example = "Juan Pérez")
        private String name;

        public UserInfo() {}

        public UserInfo(Long id, String email, String role, String name) {
            this.id = id;
            this.email = email;
            this.role = role;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // ── Constructors ──────────────────────────────────────────────────

    public AuthResponse() {}

    /**
     * Convenience constructor. The {@code refreshToken} is NOT serialised
     * to the JSON body — use {@link #setRefreshToken(String)} and read it
     * only for setting the httpOnly cookie server-side.
     */
    public AuthResponse(String accessToken, Long expiresIn, UserInfo user, String refreshToken) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.user = user;
        this.refreshToken = refreshToken;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
