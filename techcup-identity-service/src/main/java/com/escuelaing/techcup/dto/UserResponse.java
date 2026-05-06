package com.escuelaing.techcup.dto;

import com.escuelaing.techcup.model.IdentificationType;
import com.escuelaing.techcup.model.Role;
import java.time.LocalDate;

public class UserResponse {
    private String fullName;
    private String email;
    private String relationship;
    private String academicProgram;
    private Integer semester;
    private Boolean active;
    private LocalDate birthDate;
    private IdentificationType identificationType;
    private String identificationNumber;
    private Role role;

    public UserResponse() {}

    public UserResponse(String fullName, String email, String relationship, String academicProgram,
                       Integer semester, Boolean active, LocalDate birthDate,
                       IdentificationType identificationType, String identificationNumber, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.relationship = relationship;
        this.academicProgram = academicProgram;
        this.semester = semester;
        this.active = active;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
