package com.escuelaing.techcup.controller;

import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.LoginRequest;
import com.escuelaing.techcup.dto.RegisterRequest;
import com.escuelaing.techcup.exception.BusinessException;
import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IdentityController.class)
@ContextConfiguration(classes = {
        IdentityController.class,
        IdentityControllerTest.TestSecurityConfig.class,
        IdentityControllerTest.TestExceptionHandler.class
})
class IdentityControllerTest {

    private static final Logger log = LoggerFactory.getLogger(IdentityControllerTest.class);

    // Seguridad simplificada — sin InternalRequestFilter
    @Configuration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    );
            return http.build();
        }
    }

    // Manejador de excepciones para tests
    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<String> handleBusiness(BusinessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        mockAuthResponse = new AuthResponse();
        mockAuthResponse.setId(1L);
        mockAuthResponse.setToken("jwt-token");
        mockAuthResponse.setEmail("test@techcup.com");
        mockAuthResponse.setRole("PLAYER");
        mockAuthResponse.setExpiresIn(36000000L);
    }

    // ─────────────────────────────────────────────
    // POST /api/identity/login
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /login retorna 200 con token cuando las credenciales son válidas")
    void login_exitoso() throws Exception {
        LoginRequest req = new LoginRequest("test@techcup.com", "password123");
        when(authService.login(any(LoginRequest.class), any())).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/identity/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@techcup.com"))
                .andExpect(jsonPath("$.role").value("PLAYER"));
    }

    @Test
    @DisplayName("POST /login retorna 400 cuando el body es inválido")
    void login_bodyInvalido() throws Exception {
        LoginRequest req = new LoginRequest("no-es-email", "");

        mockMvc.perform(post("/api/identity/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login retorna 400 cuando las credenciales son incorrectas")
    void login_credencialesInvalidas() throws Exception {
        LoginRequest req = new LoginRequest("test@techcup.com", "wrongpass");
        when(authService.login(any(LoginRequest.class), any()))
                .thenThrow(new BusinessException("Credenciales inválidas"));

        mockMvc.perform(post("/api/identity/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Credenciales inválidas"));
    }

    // ─────────────────────────────────────────────
    // POST /api/identity/register
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /register retorna 201 con AuthResponse")
    void register_exitoso() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("nuevo@techcup.com");
        req.setPassword("password123");
        req.setRole(Role.PLAYER);

        when(authService.register(any(RegisterRequest.class), any())).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/identity/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("test@techcup.com"));
    }

    // ─────────────────────────────────────────────
    // POST /api/identity/logout
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /logout retorna 200 con usuario autenticado")
    void logout_exitoso() throws Exception {
        doNothing().when(authService).logout(any(), any());

        mockMvc.perform(post("/api/identity/logout")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sesión cerrada exitosamente"));
    }

    @Test
    @DisplayName("POST /logout retorna 403 sin autenticación")
    void logout_sinAutenticacion() throws Exception {
        mockMvc.perform(post("/api/identity/logout"))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────
    // POST /api/identity/refresh-token
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /refresh-token retorna 200 con nuevo token")
    void refreshToken_exitoso() throws Exception {
        when(authService.refeshToken(any(), any())).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/identity/refresh-token")
                        .header("Authorization", "Bearer jwt-token-expirado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    // ─────────────────────────────────────────────
    // PUT /api/identity/users/{userId}/rol
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PUT /users/{id}/rol retorna 200 con rol ADMIN")
    void cambiarRol_exitoso() throws Exception {
        doNothing().when(authService).changeRol(any(), any(), any());

        mockMvc.perform(patch("/api/identity/users/1/rol")
                        .param("newRol", "CAPTAIN")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("Rol actualizado correctamente"));
    }

    @Test
    @DisplayName("PUT /users/{id}/rol retorna 403 sin rol ADMIN")
    void cambiarRol_sinPermisos() throws Exception {
        mockMvc.perform(patch("/api/identity/users/1/rol")
                        .param("newRol", "CAPTAIN")
                        .header("X-User-Role", "PLAYER"))
                .andExpect(status().isForbidden());
    }
}