package com.escuelaing.techcup.controller;

import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.LoginRequest;
import com.escuelaing.techcup.dto.RegisterRequest;
import com.escuelaing.techcup.service.AuthService;
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
public class IdentityController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                authService.login(request, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                authService.register(request, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        authService.logout(userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok("Sesión cerrada exitosamente");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        return ResponseEntity.ok(
                authService.refreshToken(
                        userDetails.getUsername(),
                        request.getRemoteAddr()));
    }

    // Solo el ADMIN puede cambiar roles
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/rol")
    public ResponseEntity<?> cambiarRol(
            @PathVariable Long userId,
            @RequestParam String nuevoRol,
            HttpServletRequest request) {
        authService.cambiarRol(userId, nuevoRol, request.getRemoteAddr());
        return ResponseEntity.ok("Rol actualizado correctamente");
    }
}