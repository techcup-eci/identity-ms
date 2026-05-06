package com.escuelaing.techcup.dto;

import com.escuelaing.techcup.model.IdentificationType;
import com.escuelaing.techcup.model.Role;
import javax.validation.constraints.*;
import java.time.LocalDate;

public class RegisterRequest {
    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    private String relationship;

    private String academicProgram;

    @Min(1)
    @Max(12)
    private Integer semester;

    @NotNull
    private LocalDate birthDate;

    @NotNull
    private IdentificationType identificationType;

    @NotBlank
    private String identificationNumber;

    @NotNull
    private Role role; // PLAYER o INVITED

    public RegisterRequest() {}

    public RegisterRequest(String fullName, String email, String password, String relationship,
                          String academicProgram, Integer semester, LocalDate birthDate,
                          IdentificationType identificationType, String identificationNumber, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.relationship = relationship;
        this.academicProgram = academicProgram;
        this.semester = semester;
        this.birthDate = birthDate;
        this.identificationType = identificationType;
        this.identificationNumber = identificationNumber;
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getAcademicProgram() {
        return academicProgram;
    }

    public void setAcademicProgram(String academicProgram) {
        this.academicProgram = academicProgram;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public IdentificationType getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(IdentificationType identificationType) {
        this.identificationType = identificationType;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}