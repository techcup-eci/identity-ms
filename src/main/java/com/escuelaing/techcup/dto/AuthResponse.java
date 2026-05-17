package com.escuelaing.techcup.dto;

public class AuthResponse {
    private Long id;
    private String token;
    private String type = "Bearer";
    private String email;
    private String role;
    private Long expiresIn;

    public AuthResponse() {}

    public AuthResponse(String token, String email, String role, Long expiresIn) {

        this.token = token;
        this.email = email;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}