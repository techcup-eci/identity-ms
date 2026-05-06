package com.escuelaing.techcup.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class InactivateUserRequest {
    @Email
    @NotBlank
    private String email;

    public InactivateUserRequest() {}

    public InactivateUserRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}