package com.escuelaing.techcup.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class InternalRequestFilter extends OncePerRequestFilter {

    @Value("${internal.secret}")
    private String internalSecret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Rutas públicas — no necesitan el secret
        if(path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Todos los demás endpoints requieren el header secret
        String secret = request.getHeader("X-Internal-Secret");

        if (secret == null || !secret.equals(internalSecret)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Acceso no autorizado\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}