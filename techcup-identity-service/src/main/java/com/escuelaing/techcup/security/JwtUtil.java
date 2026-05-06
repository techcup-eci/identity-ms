package com.escuelaing.techcup.security;

import java.util.Date;

public class JwtUtil {
    private static final long EXPIRATION_TIME = 86400000; // 24 hours in milliseconds

    public String generateToken(String email, String role) {
        // This is a simplified implementation. In production, use a proper JWT library like jjwt
        // For now, just return a simple token format
        return "jwt_token_" + email + "_" + role + "_" + System.currentTimeMillis();
    }

    public Long getExpirationTime() {
        return EXPIRATION_TIME;
    }

    public String validateToken(String token) {
        // Simplified validation - in production use proper JWT validation
        if (token != null && token.startsWith("jwt_token_")) {
            return extractEmailFromToken(token);
        }
        return null;
    }

    private String extractEmailFromToken(String token) {
        // Simplified extraction - in production parse the JWT properly
        String[] parts = token.split("_");
        if (parts.length >= 3) {
            return parts[2];
        }
        return null;
    }
}
