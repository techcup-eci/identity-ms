package com.escuelaing.techcup.controller;

import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.LoginRequest;
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        authService.logout(userDetails.getUsername(), request.getRemoteAddr());
        return ResponseEntity.ok("Sesión cerrada exitosamente");
    }
}
