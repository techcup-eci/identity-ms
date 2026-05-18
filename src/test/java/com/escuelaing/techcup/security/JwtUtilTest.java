package com.escuelaing.techcup.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JwtUtil — validates token generation with userId, extraction of claims,
 * and security configuration alignment with orchestrator AuthFilter.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "TechCupSecretKey2024DefaultOnlyForDev");
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 900000L); // 15 min
    }

    @Test
    void generateTokenWithUserIdShouldIncludeAllClaims() {
        String token = jwtUtil.generateToken(42L, "test@escuelaing.edu.co", "PLAYER", "Test User");

        assertNotNull(token, "Token must not be null");
        assertFalse(token.isEmpty(), "Token must not be empty");
    }

    @Test
    void extractUserIdShouldReturnUserIdFromSubClaim() {
        String token = jwtUtil.generateToken(42L, "test@escuelaing.edu.co", "PLAYER", "Test User");

        String userId = jwtUtil.extractUserId(token);

        assertEquals("42", userId, "extractUserId must return the userId as String from sub claim");
    }

    @Test
    void extractEmailShouldReturnEmailClaim() {
        String token = jwtUtil.generateToken(42L, "test@escuelaing.edu.co", "PLAYER", "Test User");

        String email = jwtUtil.extractEmail(token);

        assertEquals("test@escuelaing.edu.co", email, "extractEmail must return the email claim");
    }

    @Test
    void extractRoleShouldReturnRoleClaim() {
        String token = jwtUtil.generateToken(42L, "test@escuelaing.edu.co", "CAPTAIN", "Test User");

        String role = jwtUtil.extractRole(token);

        assertEquals("CAPTAIN", role, "extractRole must return the role claim");
    }

    @Test
    void extractNameShouldReturnNameClaim() {
        String token = jwtUtil.generateToken(42L, "test@escuelaing.edu.co", "PLAYER", "Test User");

        String name = jwtUtil.extractName(token);

        assertEquals("Test User", name, "extractName must return the name claim");
    }

    @Test
    void validateTokenShouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken(42L, "test@escuelaing.edu.co", "PLAYER", "Test User");

        assertTrue(jwtUtil.validateToken(token), "validateToken must return true for a valid token");
    }

    @Test
    void validateTokenWithUsernameShouldMatchEmail() {
        String token = jwtUtil.generateToken(42L, "test@escuelaing.edu.co", "PLAYER", "Test User");

        assertTrue(jwtUtil.validateToken(token, "test@escuelaing.edu.co"),
                "validateToken with matching username must return true");
    }

    @Test
    void validateTokenWithWrongUsernameShouldFail() {
        String token = jwtUtil.generateToken(42L, "test@escuelaing.edu.co", "PLAYER", "Test User");

        assertFalse(jwtUtil.validateToken(token, "wrong@email.com"),
                "validateToken with wrong username must return false");
    }

    @Test
    void legacyGenerateTokenShouldStillWork() {
        String token = jwtUtil.generateToken("legacy@escuelaing.edu.co", "PLAYER");

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("legacy@escuelaing.edu.co", jwtUtil.extractEmail(token));
        assertEquals("PLAYER", jwtUtil.extractRole(token));
    }

    @Test
    void differentUserIdsShouldProduceDifferentTokens() {
        String token1 = jwtUtil.generateToken(1L, "a@b.com", "PLAYER", "User A");
        String token2 = jwtUtil.generateToken(2L, "a@b.com", "PLAYER", "User A");

        assertNotEquals(token1, token2, "Tokens for different userIds must be different");
    }

    @Test
    void expiredTokenShouldFailValidation() throws InterruptedException {
        // Set expiration to 1ms
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 1L);
        String token = jwtUtil.generateToken(42L, "test@escuelaing.edu.co", "PLAYER", "Test User");

        // Wait for token to expire
        Thread.sleep(10);

        assertFalse(jwtUtil.validateToken(token), "Expired token must fail validation");
    }

    // --- Refresh token generation tests ---

    @Test
    void generateRefreshTokenShouldReturnNonNullPair() {
        JwtUtil.TokenPair pair = jwtUtil.generateRefreshToken();

        assertNotNull(pair, "TokenPair must not be null");
        assertNotNull(pair.getRaw(), "Raw token must not be null");
        assertNotNull(pair.getHashed(), "Hashed token must not be null");
    }

    @Test
    void generateRefreshTokenShouldProduceUuidFormatRawToken() {
        JwtUtil.TokenPair pair = jwtUtil.generateRefreshToken();

        assertTrue(pair.getRaw().matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
                "Raw token must be UUID format");
    }

    @Test
    void generateRefreshTokenShouldHashWithSha256() {
        JwtUtil.TokenPair pair = jwtUtil.generateRefreshToken();
        String expectedHash = JwtUtil.sha256Hex(pair.getRaw());

        assertEquals(64, pair.getHashed().length(),
                "SHA-256 hash must be 64 hex characters");
        assertEquals(expectedHash, pair.getHashed(),
                "Hashed value must match SHA-256 of raw token");
    }

    @Test
    void generateRefreshTokenShouldProduceDifferentTokensOnEachCall() {
        JwtUtil.TokenPair pair1 = jwtUtil.generateRefreshToken();
        JwtUtil.TokenPair pair2 = jwtUtil.generateRefreshToken();

        assertNotEquals(pair1.getRaw(), pair2.getRaw(),
                "Raw tokens must be unique on each call");
        assertNotEquals(pair1.getHashed(), pair2.getHashed(),
                "Hashed tokens must be unique on each call");
    }
}
