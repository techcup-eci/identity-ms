package com.escuelaing.techcup.controller;

import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.LoginRequest;
import com.escuelaing.techcup.dto.RegisterRequest;
import com.escuelaing.techcup.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/identity")
@Tag(name = "Identity", description = "API de autenticación y gestión de identidad")
public class IdentityController {

    @Autowired
    private AuthService authService;

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica al usuario con email y contraseña, retorna un token JWT"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",
                    content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                authService.login(request, httpRequest.getRemoteAddr()));
    }

    @Operation(
            summary = "Registrar usuario",
            description = "Crea una nueva cuenta de usuario y retorna un token JWT"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                authService.register(request, httpRequest.getRemoteAddr()));
    }

    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida la sesión del usuario autenticado",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado",
                    content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("X-User-Id") String email,
            HttpServletRequest request) {
        authService.logout(email, request.getRemoteAddr());
        return ResponseEntity.ok("Sesión cerrada exitosamente");
    }

    @Operation(
            summary = "Refrescar token",
            description = "Genera un nuevo token JWT para el usuario autenticado",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado",
                    content = @Content)
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("X-User-Id") String email,
            HttpServletRequest request) {
        return ResponseEntity.ok(
                authService.refreshToken(email, request.getRemoteAddr()));
    }

    @Operation(
            summary = "Cambiar rol de usuario",
            description = "Permite a un administrador cambiar el rol de un usuario",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Rol inválido",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{userId}/rol")
    public ResponseEntity<?> cambiarRol(
            @PathVariable Long userId,
            @RequestParam String nuevoRol,
            @RequestHeader("X-User-Role") String userRole,
            HttpServletRequest request) {
        if (!"ROLE_ADMIN".equals(userRole) && !"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo el administrador puede cambiar roles");
        }
        authService.cambiarRol(userId, nuevoRol, request.getRemoteAddr());
        return ResponseEntity.ok("Rol actualizado correctamente");
    }
}