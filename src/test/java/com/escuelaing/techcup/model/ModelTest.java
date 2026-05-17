package com.escuelaing.techcup.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    // ─────────────────────────────────────────────
    // User
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("User constructor vacío y setters funcionan correctamente")
    void user_settersYGetters() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@techcup.com");
        user.setPassword("encoded_password");
        user.setActive(true);
        user.setRole(Role.PLAYER);

        assertEquals(1L, user.getId());
        assertEquals("test@techcup.com", user.getEmail());
        assertEquals("encoded_password", user.getPassword());
        assertTrue(user.getActive());
        assertEquals(Role.PLAYER, user.getRole());
    }

    @Test
    @DisplayName("User tiene rol INVITED y activo por defecto")
    void user_valoresPorDefecto() {
        User user = new User();
        assertEquals(Role.INVITED, user.getRole());
        assertTrue(user.getActive());
    }

    @Test
    @DisplayName("User acepta todos los roles disponibles")
    void user_todosLosRoles() {
        User user = new User();
        for (Role role : Role.values()) {
            user.setRole(role);
            assertEquals(role, user.getRole());
        }
    }

    @Test
    @DisplayName("User puede ser inactivado")
    void user_inactivar() {
        User user = new User();
        user.setActive(false);
        assertFalse(user.getActive());
    }

    // ─────────────────────────────────────────────
    // AuditLog
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("AuditLog constructor vacío y setters funcionan correctamente")
    void auditLog_settersYGetters() {
        AuditLog log = new AuditLog();
        LocalDateTime now = LocalDateTime.now();

        log.setId(1L);
        log.setAction("LOGIN");
        log.setUserEmail("test@techcup.com");
        log.setDetails("Inicio de sesión exitoso");
        log.setIpAddress("127.0.0.1");
        log.setTimestamp(now);

        assertEquals(1L, log.getId());
        assertEquals("LOGIN", log.getAction());
        assertEquals("test@techcup.com", log.getUserEmail());
        assertEquals("Inicio de sesión exitoso", log.getDetails());
        assertEquals("127.0.0.1", log.getIpAddress());
        assertEquals(now, log.getTimestamp());
    }

    @Test
    @DisplayName("AuditLog tiene timestamp por defecto al crearse")
    void auditLog_timestampPorDefecto() {
        LocalDateTime antes = LocalDateTime.now();
        AuditLog log = new AuditLog();
        LocalDateTime despues = LocalDateTime.now();

        assertNotNull(log.getTimestamp());
        assertTrue(!log.getTimestamp().isBefore(antes));
        assertTrue(!log.getTimestamp().isAfter(despues));
    }

    @Test
    @DisplayName("AuditLog acepta todas las acciones de auditoría")
    void auditLog_todasLasAcciones() {
        AuditLog log = new AuditLog();
        String[] acciones = {"LOGIN", "LOGOUT", "REGISTER", "CAMBIO_ROL", "REFRESH_TOKEN"};

        for (String accion : acciones) {
            log.setAction(accion);
            assertEquals(accion, log.getAction());
        }
    }
}