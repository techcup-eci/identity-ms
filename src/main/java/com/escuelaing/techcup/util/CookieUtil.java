package com.escuelaing.techcup.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * Utility for building httpOnly cookies for refresh tokens.
 *
 * Cookie settings:
 * - Name:     refresh_token
 * - Path:     /api/identity/refresh — restringido solo al endpoint de refresh
 *             para minimizar la exposición del token en otras rutas.
 * - HttpOnly: true (JS no puede acceder — protección XSS)
 * - Secure:   false en dev (true en producción con HTTPS)
 * - SameSite: Strict (protección CSRF)
 * - MaxAge:   604800 segundos (7 días)
 */
public final class CookieUtil {

    private static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/identity/refresh";
    private static final long MAX_AGE_SECONDS = Duration.ofDays(7).getSeconds();

    private CookieUtil() {}

    public static ResponseCookie buildRefreshTokenCookie(String rawRefreshToken) {
        return ResponseCookie.from(COOKIE_NAME, rawRefreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path(COOKIE_PATH)
                .maxAge(Duration.ofSeconds(MAX_AGE_SECONDS))
                .build();
    }

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
