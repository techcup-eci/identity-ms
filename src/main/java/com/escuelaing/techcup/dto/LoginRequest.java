package com.escuelaing.techcup.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Schema(description = "Login request payload with email and password credentials")
public class LoginRequest {

    @Schema(description = "User email address", example = "admin@techcup.com", required = true)
    @Email
    @NotBlank
    private String email;

    @Schema(description = "User password (min 8 characters)", example = "admin123", required = true,
            minLength = 8)
    @NotBlank
    private String password;

    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
