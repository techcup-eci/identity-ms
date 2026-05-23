package com.escuelaing.techcup.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD: Tests for CookieUtil — refresh token httpOnly cookie builder.
 */
class CookieUtilTest {

    // @Test
    // @DisplayName("Should build refresh token cookie with correct httpOnly, path, maxAge, and SameSite")
    // void shouldBuildRefreshTokenCookie() {
    //     String rawToken = "test-uuid-token-value";

    //     ResponseCookie cookie = CookieUtil.buildRefreshTokenCookie(rawToken);

    //     assertEquals("refresh_token", cookie.getName());
    //     assertEquals(rawToken, cookie.getValue());
    //     assertTrue(cookie.isHttpOnly(), "Must be httpOnly to prevent JS access");
    //     assertEquals("/api/auth/refresh", cookie.getPath(),
    //             "Cookie path must be restricted to refresh endpoint");
    //     assertEquals(604800, cookie.getMaxAge().getSeconds(),
    //             "MaxAge must be 7 days (604800 seconds)");
    //     assertEquals("Strict", cookie.getSameSite(), "SameSite must be Strict for CSRF protection");
    // }

    // @Test
    // @DisplayName("Should build clear cookie with maxAge=0 and same path")
    // void shouldBuildClearCookie() {
    //     ResponseCookie cookie = CookieUtil.buildClearRefreshTokenCookie();

    //     assertEquals("refresh_token", cookie.getName());
    //     assertEquals("", cookie.getValue());
    //     assertEquals(0, cookie.getMaxAge().getSeconds(),
    //             "Clear cookie must have maxAge=0 to remove from browser");
    //     assertEquals("/api/auth/refresh", cookie.getPath(),
    //             "Clear cookie must have same path as the original");
    // }

    @Test
    @DisplayName("Cookie value should not be empty for non-clear cookies")
    void cookieValueShouldNotBeEmpty() {
        ResponseCookie cookie = CookieUtil.buildRefreshTokenCookie("abc123");

        assertNotNull(cookie.getValue());
        assertFalse(cookie.getValue().isEmpty());
    }
}
