package com.escuelaing.techcup.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD: Tests for RefreshToken JPA entity.
 * Validates entity structure, default values, and data contract.
 */
class RefreshTokenTest {

    @Test
    @DisplayName("New RefreshToken should default revoked to false")
    void newRefreshTokenShouldDefaultRevokedToFalse() {
        RefreshToken token = new RefreshToken();

        assertFalse(token.isRevoked(),
                "New refresh token must default revoked to false");
    }

    @Test
    @DisplayName("Should persist and retrieve all fields correctly")
    void shouldSetAndGetAllFields() {
        RefreshToken token = new RefreshToken();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusDays(7);
        String hash = "d1d8a5c7e3f2b6a9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7";

        token.setId(1L);
        token.setTokenHash(hash);
        token.setUserId(42L);
        token.setUsersMsUserId(99L);
        token.setCreatedAt(now);
        token.setExpiresAt(expires);
        token.setRevoked(true);

        assertEquals(1L, token.getId());
        assertEquals(hash, token.getTokenHash());
        assertEquals(42L, token.getUserId());
        assertEquals(99L, token.getUsersMsUserId());
        assertEquals(now, token.getCreatedAt());
        assertEquals(expires, token.getExpiresAt());
        assertTrue(token.isRevoked());
    }

    @Test
    @DisplayName("Should store SHA-256 hash (64 hex characters) in tokenHash")
    void shouldStoreSha256Hash() {
        RefreshToken token = new RefreshToken();
        String sha256Hash = "a".repeat(64); // 64 hex chars

        token.setTokenHash(sha256Hash);

        assertEquals(64, token.getTokenHash().length(),
                "SHA-256 hash must be 64 characters");
        assertEquals(sha256Hash, token.getTokenHash());
    }

    @Test
    @DisplayName("expiresAt should be exactly 7 days after createdAt when constructed")
    void expiresAtShouldBeSevenDaysAfterCreatedAt() {
        RefreshToken token = new RefreshToken();
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 17, 10, 0, 0);
        LocalDateTime expectedExpiry = LocalDateTime.of(2026, 5, 24, 10, 0, 0);

        token.setCreatedAt(createdAt);
        token.setExpiresAt(expectedExpiry);

        assertEquals(createdAt, token.getCreatedAt());
        assertEquals(expectedExpiry, token.getExpiresAt());
        assertEquals(7,
                java.time.Duration.between(createdAt, token.getExpiresAt()).toDays());
    }
}
