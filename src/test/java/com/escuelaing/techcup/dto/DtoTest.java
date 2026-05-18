package com.escuelaing.techcup.dto;

import com.escuelaing.techcup.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    // ─────────────────────────────────────────────
    // AuthResponse
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("AuthResponse constructor vacío y setters funcionan correctamente")
    void authResponse_settersYGetters() {
        AuthResponse r = new AuthResponse();
        r.setId(1L);
        r.setToken("jwt-token");
        r.setType("Bearer");
        r.setEmail("test@techcup.com");
        r.setRole("PLAYER");
        r.setExpiresIn(36000000L);

        assertEquals(1L, r.getId());
        assertEquals("jwt-token", r.getToken());
        assertEquals("Bearer", r.getType());
        assertEquals("test@techcup.com", r.getEmail());
        assertEquals("PLAYER", r.getRole());
        assertEquals(36000000L, r.getExpiresIn());
    }

    @Test
    @DisplayName("AuthResponse constructor con parámetros funciona correctamente")
    void authResponse_constructorConParametros() {
        AuthResponse r = new AuthResponse("jwt-token", "test@techcup.com", "ADMIN", 36000000L);

        assertEquals("jwt-token", r.getToken());
        assertEquals("test@techcup.com", r.getEmail());
        assertEquals("ADMIN", r.getRole());
        assertEquals(36000000L, r.getExpiresIn());
        assertEquals("Bearer", r.getType()); // valor por defecto
    }

    // ─────────────────────────────────────────────
    // LoginRequest
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("LoginRequest constructor vacío y setters funcionan correctamente")
    void loginRequest_settersYGetters() {
        LoginRequest r = new LoginRequest();
        r.setEmail("test@techcup.com");
        r.setPassword("password123");

        assertEquals("test@techcup.com", r.getEmail());
        assertEquals("password123", r.getPassword());
    }

    @Test
    @DisplayName("LoginRequest constructor con parámetros funciona correctamente")
    void loginRequest_constructorConParametros() {
        LoginRequest r = new LoginRequest("test@techcup.com", "password123");

        assertEquals("test@techcup.com", r.getEmail());
        assertEquals("password123", r.getPassword());
    }

    // ─────────────────────────────────────────────
    // RegisterRequest
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("RegisterRequest setters y getters funcionan correctamente")
    void registerRequest_settersYGetters() {
        RegisterRequest r = new RegisterRequest();
        r.setEmail("nuevo@techcup.com");
        r.setPassword("pass123");
        r.setRole(Role.CAPTAIN);

        assertEquals("nuevo@techcup.com", r.getEmail());
        assertEquals("pass123", r.getPassword());
        assertEquals(Role.CAPTAIN, r.getRole());
    }

    // ─────────────────────────────────────────────
    // UserServiceResponse
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("UserServiceResponse setters y getters funcionan correctamente")
    void userServiceResponse_settersYGetters() {
        UserServiceResponse r = new UserServiceResponse();
        r.setId(10L);
        r.setName("Juan Pérez");
        r.setEmail("juan@techcup.com");
        r.setBirthDate("1995-05-15");
        r.setRelationship("SINGLE");
        r.setIdentificationType("CC");
        r.setPhone("3001234567");
        r.setIdentificationNumber("123456789");
        r.setRol("PLAYER");

        assertEquals(10L, r.getId());
        assertEquals("Juan Pérez", r.getName());
        assertEquals("juan@techcup.com", r.getEmail());
        assertEquals("1995-05-15", r.getBirthDate());
        assertEquals("SINGLE", r.getRelationship());
        assertEquals("CC", r.getIdentificationType());
        assertEquals("3001234567", r.getPhone());
        assertEquals("123456789", r.getIdentificationNumber());
        assertEquals("PLAYER", r.getRol());
    }
}