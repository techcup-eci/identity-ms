package com.escuelaing.techcup.dto;

import com.escuelaing.techcup.model.Role;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

public class UpdateRoleRequest {
    @Email
    @NotBlank
    private String email;

    @NotNull
    private Role newRole;

    public UpdateRoleRequest() {}

    public UpdateRoleRequest(String email, Role newRole) {
        this.email = email;
        this.newRole = newRole;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getNewRole() {
        return newRole;
    }

    public void setNewRole(Role newRole) {
        this.newRole = newRole;
    }
}