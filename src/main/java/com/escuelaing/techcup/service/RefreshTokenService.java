package com.escuelaing.techcup.service;

import com.escuelaing.techcup.exception.BusinessException;
import com.escuelaing.techcup.model.RefreshToken;
import com.escuelaing.techcup.repository.RefreshTokenRepository;
import com.escuelaing.techcup.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for refresh token lifecycle management: creation, revocation, and rotation.
 * Refresh tokens are stored as SHA-256 hashes — raw tokens never hit the database.
 */
@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Result of a token rotation operation, containing the newly created refresh token
     * entity (with userId for JWT generation) and the raw token to set as httpOnly cookie.
     */
    public static class RotationResult {
        private final RefreshToken entity;
        private final String rawToken;

        public RotationResult(RefreshToken entity, String rawToken) {
            this.entity = entity;
            this.rawToken = rawToken;
        }

        public RefreshToken getEntity() { return entity; }
        public String getRawToken() { return rawToken; }
    }

    /**
     * Creates a new refresh token for a user.
     * Generates a random UUID, hashes it with SHA-256, persists the hash,
     * and returns the RAW token to be sent to the client as an httpOnly cookie.
     *
     * @param userId         the identity-ms User ID
     * @param usersMsUserId  cross-service link to users-and-players-ms
     * @return raw refresh token string (UUID format)
     */
    @Transactional
    public String createRefreshToken(Long userId, Long usersMsUserId) {
        JwtUtil.TokenPair pair = jwtUtil.generateRefreshToken();

        RefreshToken token = new RefreshToken();
        token.setTokenHash(pair.getHashed());
        token.setUserId(userId);
        token.setUsersMsUserId(usersMsUserId);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        token.setRevoked(false);

        refreshTokenRepository.save(token);

        return pair.getRaw();
    }

    /**
     * Revokes all refresh tokens for a given user.
     * Used during logout to invalidate all existing refresh tokens.
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    /**
     * Rotates a refresh token: validates the existing token, revokes it,
     * creates a new one, and returns both the new entity and raw token.
     *
     * Token rotation prevents refresh token reuse — each refresh invalidates the old token.
     * If a stolen refresh token is attempted after rotation, it will be found revoked.
     *
     * @param rawToken the raw refresh token from the httpOnly cookie
     * @return RotationResult containing the new RefreshToken entity and raw token
     * @throws BusinessException if token not found, revoked, or expired
     */
    @Transactional
    public RotationResult rotateRefreshToken(String rawToken) {
        String hash = JwtUtil.sha256Hex(rawToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException("Token de refresco inválido o expirado"));

        if (existing.isRevoked()) {
            throw new BusinessException("Token de refresco inválido o expirado");
        }

        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token de refresco inválido o expirado");
        }

        // Revoke the old token
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        // Create a new refresh token for the same user
        JwtUtil.TokenPair newPair = jwtUtil.generateRefreshToken();

        RefreshToken newToken = new RefreshToken();
        newToken.setTokenHash(newPair.getHashed());
        newToken.setUserId(existing.getUserId());
        newToken.setUsersMsUserId(existing.getUsersMsUserId());
        newToken.setCreatedAt(LocalDateTime.now());
        newToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        newToken.setRevoked(false);

        RefreshToken saved = refreshTokenRepository.save(newToken);
        return new RotationResult(saved, newPair.getRaw());
    }
}
