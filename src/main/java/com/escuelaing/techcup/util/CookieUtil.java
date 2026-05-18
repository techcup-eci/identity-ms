package com.escuelaing.techcup.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * Utility for building httpOnly cookies for refresh tokens.
 * 
 * Cookie settings (per design):
 * - Name: refresh_token
 * - Path: / (sent on all requests — the gateway rewrites /api/auth/* → /api/identity/*)
 * - HttpOnly: true (JS cannot access — XSS-safe)
 * - Secure: false in dev (set to true in production)
 * - SameSite: Strict (CSRF protection)
 * - MaxAge: 604800 seconds (7 days)
 */
public final class CookieUtil {

    private static final String COOKIE_NAME = "refresh_token";
    // Use "/" so the browser sends the cookie on /api/auth/refresh (gateway path)
    // and /api/auth/login, /api/auth/register, etc.
    private static final String COOKIE_PATH = "/";
    private static final long MAX_AGE_SECONDS = Duration.ofDays(7).getSeconds();

    private CookieUtil() {
        // Utility class — prevent instantiation
    }

    /**
     * Builds a refresh token cookie to be set on login, register, or refresh responses.
     * The raw token is stored in an httpOnly cookie, inaccessible to JavaScript.
     *
     * @param rawRefreshToken the raw UUID refresh token string
     * @return ResponseCookie ready to be added to the Set-Cookie header
     */
    public static ResponseCookie buildRefreshTokenCookie(String rawRefreshToken) {
        return ResponseCookie.from(COOKIE_NAME, rawRefreshToken)
                .httpOnly(true)
                .secure(false) // true in production (HTTPS)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(Duration.ofSeconds(MAX_AGE_SECONDS))
                .build();
    }

    /**
     * Builds a "clear" cookie that instructs the browser to delete the refresh token cookie.
     * Used on logout to remove the cookie from the client.
     *
     * @return ResponseCookie with maxAge=0 and same path as the original
     */
    public static ResponseCookie buildClearRefreshTokenCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
