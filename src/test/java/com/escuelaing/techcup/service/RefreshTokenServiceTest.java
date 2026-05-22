package com.escuelaing.techcup.service;

import com.escuelaing.techcup.model.RefreshToken;
import com.escuelaing.techcup.repository.RefreshTokenRepository;
import com.escuelaing.techcup.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * TDD: Tests for RefreshTokenService.
 * Validates token creation, revocation, and rotation logic.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Nested
    @DisplayName("createRefreshToken")
    class CreateRefreshToken {

        @Test
        @DisplayName("Should generate unique token, hash it, persist, and return raw token")
        void shouldCreateAndPersistRefreshToken() {
            String rawUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
            String hashedUuid = JwtUtil.sha256Hex(rawUuid);
            when(jwtUtil.generateRefreshToken())
                    .thenReturn(new JwtUtil.TokenPair(rawUuid, hashedUuid));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
                RefreshToken t = inv.getArgument(0);
                t.setId(1L);
                return t;
            });

            String rawToken = refreshTokenService.createRefreshToken(42L, 99L);

            assertNotNull(rawToken, "Raw token must not be null");
            assertEquals(rawUuid, rawToken, "Raw token must match generated UUID");

            // Verify the saved entity has correct properties
            verify(refreshTokenRepository).save(argThat(token ->
                    token.getUserId() == 42L
                    && token.getUsersMsUserId() == 99L
                    && token.getTokenHash() != null
                    && token.getTokenHash().length() == 64
                    && token.getCreatedAt() != null
                    && token.getExpiresAt() != null
                    && !token.isRevoked()
            ));
        }

        @Test
        @DisplayName("Should generate different tokens for same user on subsequent calls")
        void shouldGenerateDifferentTokens() {
            String raw1 = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
            String raw2 = "b2c3d4e5-f6a7-8901-bcde-f12345678901";
            when(jwtUtil.generateRefreshToken())
                    .thenReturn(new JwtUtil.TokenPair(raw1, JwtUtil.sha256Hex(raw1)))
                    .thenReturn(new JwtUtil.TokenPair(raw2, JwtUtil.sha256Hex(raw2)));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
                RefreshToken t = inv.getArgument(0);
                t.setId(1L);
                return t;
            });

            String token1 = refreshTokenService.createRefreshToken(42L, 99L);
            String token2 = refreshTokenService.createRefreshToken(42L, 99L);

            assertNotEquals(token1, token2,
                    "Subsequent calls must produce different raw tokens");
        }

        @Test
        @DisplayName("Should store SHA-256 hash of the raw token, not the raw token itself")
        void shouldStoreHashNotRawToken() {
            String rawUuid = "c3d4e5f6-a7b8-9012-cdef-234567890123";
            String expectedHash = JwtUtil.sha256Hex(rawUuid);
            when(jwtUtil.generateRefreshToken())
                    .thenReturn(new JwtUtil.TokenPair(rawUuid, expectedHash));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
                RefreshToken t = inv.getArgument(0);
                t.setId(1L);
                return t;
            });

            String rawToken = refreshTokenService.createRefreshToken(42L, 99L);

            verify(refreshTokenRepository).save(argThat(token ->
                    token.getTokenHash().equals(expectedHash)
            ));
        }
    }

    @Nested
    @DisplayName("revokeAllUserTokens")
    class RevokeAllUserTokens {

        @Test
        @DisplayName("Should call repository revokeAllByUserId")
        void shouldDelegateToRepository() {
            when(refreshTokenRepository.revokeAllByUserId(42L)).thenReturn(3);

            refreshTokenService.revokeAllUserTokens(42L);

            verify(refreshTokenRepository).revokeAllByUserId(42L);
        }
    }

    @Nested
    @DisplayName("rotateRefreshToken")
    class RotateRefreshToken {

        @Test
        @DisplayName("Should revoke old token and create new one, returning valid refresh token")
        void shouldRotateTokenSuccessfully() {
            String rawOldToken = "d4e5f6a7-b8c9-0123-defa-345678901234";
            String oldHash = JwtUtil.sha256Hex(rawOldToken);

            // Existing token in DB
            RefreshToken oldToken = new RefreshToken();
            oldToken.setId(1L);
            oldToken.setTokenHash(oldHash);
            oldToken.setUserId(42L);
            oldToken.setUsersMsUserId(99L);
            oldToken.setCreatedAt(LocalDateTime.now().minusDays(1));
            oldToken.setExpiresAt(LocalDateTime.now().plusDays(6));
            oldToken.setRevoked(false);

            // New token pair for rotation
            String newRaw = "e5f6a7b8-c9d0-1234-efab-456789012345";
            String newHash = JwtUtil.sha256Hex(newRaw);

            when(refreshTokenRepository.findByTokenHash(oldHash))
                    .thenReturn(Optional.of(oldToken));
            when(jwtUtil.generateRefreshToken())
                    .thenReturn(new JwtUtil.TokenPair(newRaw, newHash));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
                RefreshToken t = inv.getArgument(0);
                if (t.getTokenHash().equals(newHash)) t.setId(2L); // new token gets ID 2
                return t;
            });

            RefreshTokenService.RotationResult result = refreshTokenService.rotateRefreshToken(rawOldToken);

            // Old token should be revoked
            assertTrue(oldToken.isRevoked(), "Old token must be marked revoked");
            verify(refreshTokenRepository).save(oldToken); // save old as revoked

            // New token properties
            RefreshToken newTokenEntity = result.getEntity();
            assertNotNull(newTokenEntity, "Result entity must not be null");
            assertEquals(42L, newTokenEntity.getUserId(), "New token must have same userId");
            assertEquals(99L, newTokenEntity.getUsersMsUserId(), "New token must have same usersMsUserId");
            assertNotEquals(oldHash, newTokenEntity.getTokenHash(), "New token must have different hash");
            assertFalse(newTokenEntity.isRevoked(), "New token must not be revoked");

            // Raw token should be a valid non-empty UUID
            assertNotNull(result.getRawToken(), "Raw token must not be null");
            assertFalse(result.getRawToken().isEmpty(), "Raw token must not be empty");
        }

        @Test
        @DisplayName("Should throw exception when token does not exist in DB")
        void shouldFailWhenTokenNotFound() {
            when(refreshTokenRepository.findByTokenHash(anyString()))
                    .thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () ->
                    refreshTokenService.rotateRefreshToken("non-existent-token"));
        }

        @Test
        @DisplayName("Should throw exception when token is already revoked")
        void shouldFailWhenTokenRevoked() {
            String rawToken = "revoked-token-uuid";
            String hash = JwtUtil.sha256Hex(rawToken);

            RefreshToken revoked = new RefreshToken();
            revoked.setId(1L);
            revoked.setTokenHash(hash);
            revoked.setUserId(42L);
            revoked.setRevoked(true);
            revoked.setExpiresAt(LocalDateTime.now().plusDays(6));

            when(refreshTokenRepository.findByTokenHash(hash))
                    .thenReturn(Optional.of(revoked));

            assertThrows(RuntimeException.class, () ->
                    refreshTokenService.rotateRefreshToken(rawToken));
        }

        @Test
        @DisplayName("Should throw exception when token is expired")
        void shouldFailWhenTokenExpired() {
            String rawToken = "expired-token-uuid";
            String hash = JwtUtil.sha256Hex(rawToken);

            RefreshToken expired = new RefreshToken();
            expired.setId(1L);
            expired.setTokenHash(hash);
            expired.setUserId(42L);
            expired.setRevoked(false);
            expired.setExpiresAt(LocalDateTime.now().minusDays(1)); // in the past

            when(refreshTokenRepository.findByTokenHash(hash))
                    .thenReturn(Optional.of(expired));

            assertThrows(RuntimeException.class, () ->
                    refreshTokenService.rotateRefreshToken(rawToken));
        }
    }
}
