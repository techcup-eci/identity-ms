package com.escuelaing.techcup.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expirationTime;

    /**
     * Holds a raw token (sent to client) and its SHA-256 hash (stored in DB).
     */
    public static class TokenPair {
        private final String raw;
        private final String hashed;

        public TokenPair(String raw, String hashed) {
            this.raw = raw;
            this.hashed = hashed;
        }

        public String getRaw() { return raw; }
        public String getHashed() { return hashed; }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @deprecated Use {@link #generateToken(Long, String, String, String)} instead,
     *             which includes userId (as sub) and name claims.
     */
    @Deprecated
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generates a JWT access token with userId as subject, plus email, role, and name claims.
     * The orchestrator AuthFilter reads sub as X-User-Id and role as X-User-Role.
     */
    public String generateToken(Long userId, String email, String role, String name) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .claim("name", name)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts email from token. In legacy tokens, email is the subject.
     * In new tokens, email is a separate claim and subject is userId.
     */
    public String extractEmail(String token) {
        Claims claims = getClaims(token);
        String email = claims.get("email", String.class);
        return email != null ? email : claims.getSubject();
    }

    public String extractUserId(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String extractName(String token) {
        return getClaims(token).get("name", String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateToken(String token, String username) {
        return validateToken(token) && username.equals(extractEmail(token));
    }

    /**
     * Generates a random UUID-based refresh token and returns both the raw token
     * (sent to the client via httpOnly cookie) and its SHA-256 hash (stored in DB).
     * The raw token is NEVER stored — only the hash.
     */
    public TokenPair generateRefreshToken() {
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = sha256Hex(rawToken);
        return new TokenPair(rawToken, hashedToken);
    }

    /**
     * Computes SHA-256 hash of a string and returns it as a lowercase hex string.
     */
    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}