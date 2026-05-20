package com.escuelaing.techcup.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey",
                "TechCupSecretKey2024VerySecureLongKeyForJWT");
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 36000000L);
    }

    @Test
    @DisplayName("generateToken crea un token no nulo")
    void generateToken_noNulo() {
        String token = jwtUtil.generateToken("1","user@techcup.com", "PLAYER");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("extractEmail extrae el email correcto del token")
    void extractEmail_correcto() {
        String token = jwtUtil.generateToken("1","user@techcup.com", "PLAYER");
        assertEquals("user@techcup.com", jwtUtil.extractEmail(token));
    }

    @Test
    @DisplayName("extractRole extrae el rol correcto del token")
    void extractRole_correcto() {
        String token = jwtUtil.generateToken("1","user@techcup.com", "ADMIN");
        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    @DisplayName("validateToken retorna true para un token válido")
    void validateToken_valido() {
        String token = jwtUtil.generateToken("1","user@techcup.com", "PLAYER");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("validateToken retorna false para un token inválido")
    void validateToken_invalido() {
        assertFalse(jwtUtil.validateToken("token.invalido.aqui"));
    }

    @Test
    @DisplayName("validateToken con username retorna true cuando coincide")
    void validateToken_conUserId_coincide() {
        String token = jwtUtil.generateToken("1","user@techcup.com", "PLAYER");
        assertTrue(jwtUtil.validateToken(token, "1"));
    }

    @Test
    @DisplayName("validateToken con username retorna false cuando no coincide")
    void validateToken_conUserId_noCoincide() {
        String token = jwtUtil.generateToken("1","user@techcup.com", "PLAYER");
        assertFalse(jwtUtil.validateToken(token, "555"));
    }

    @Test
    @DisplayName("getExpirationTime retorna el valor configurado")
    void getExpirationTime_correcto() {
        assertEquals(36000000L, jwtUtil.getExpirationTime());
    }

    @Test
    @DisplayName("token expirado no es válido")
    void validateToken_expirado() {
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", -1000L);
        String token = jwtUtil.generateToken("1","user@techcup.com", "PLAYER");
        assertFalse(jwtUtil.validateToken(token));
    }
}
