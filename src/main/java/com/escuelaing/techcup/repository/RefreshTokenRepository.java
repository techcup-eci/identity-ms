package com.escuelaing.techcup.repository;

import com.escuelaing.techcup.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for RefreshToken entity.
 * Provides custom query methods for token lookup, revocation, and cleanup.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its SHA-256 hash.
     * Used during refresh to look up the stored token.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Find all active (non-revoked) refresh tokens for a given user.
     * Used during token rotation to identify tokens to revoke.
     */
    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

    /**
     * Revoke all tokens for a user by setting revoked = true.
     * Used during logout to invalidate all refresh tokens.
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.userId = :userId")
    int revokeAllByUserId(@Param("userId") Long userId);

    /**
     * Delete all tokens for a user.
     * Used for cleanup operations.
     */
    void deleteByUserId(Long userId);
}
