package com.escuelaing.techcup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a refresh token stored in the database.
 * The tokenHash stores a SHA-256 hash of the raw UUID token sent to the client.
 * The raw token is NEVER stored — only the hash.
 *
 * Rotation policy: on each refresh, the old token is revoked and a new one is created.
 * Logout revokes ALL tokens for the user.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", unique = true, nullable = false, length = 64)
    private String tokenHash;       // SHA-256 hash of raw UUID token (64 hex chars)

    @Column(name = "user_id", nullable = false)
    private Long userId;            // identity-ms user ID (FK to users table)

    @Column(name = "users_ms_user_id")
    private Long usersMsUserId;     // cross-service link to users-and-players-ms

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // createdAt + 7 days

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    public RefreshToken() {}

    // --- Getters and setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUsersMsUserId() {
        return usersMsUserId;
    }

    public void setUsersMsUserId(Long usersMsUserId) {
        this.usersMsUserId = usersMsUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
