package com.escuelaing.techcup.controller;

import com.escuelaing.techcup.dto.*;
import com.escuelaing.techcup.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.login(request, ipAddress));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        authService.logout(userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok("Sesión cerrada exitosamente");
    }

    @PutMapping("/role")
    public ResponseEntity<?> updateRole(@AuthenticationPrincipal UserDetails adminDetails,
                                         @Valid @RequestBody UpdateRoleRequest request) {
        authService.updateRole(adminDetails.getUsername(), request);
        return ResponseEntity.ok("Rol actualizado exitosamente");
    }

    @PutMapping("/inactivate")
    public ResponseEntity<?> inactivateUser(@AuthenticationPrincipal UserDetails adminDetails,
                                             @Valid @RequestBody InactivateUserRequest request) {
        authService.inactivateUser(adminDetails.getUsername(), request);
        return ResponseEntity.ok("Usuario inactivado exitosamente");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getUserInfo(userDetails.getUsername()));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<UserResponse> getUserInfo(@AuthenticationPrincipal UserDetails userDetails,
                                                     @PathVariable String email) {
        // Solo ADMIN puede ver otros usuarios
        UserResponse response = authService.getUserInfo(email);
        return ResponseEntity.ok(response);
    }
}