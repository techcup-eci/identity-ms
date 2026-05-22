package com.escuelaing.techcup.dto;

import com.escuelaing.techcup.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Registration request with full user profile information. All fields are required unless marked optional.")
public class RegisterRequest {

    @Schema(description = "Valid email address (institutional or personal)",
            example = "estudiante@escuelaing.edu.co", required = true)
    @Email(message = "Debe proporcionar un correo electrónico válido")
    @NotBlank(message = "El correo electrónico es requerido")
    private String email;

    @Schema(description = "Password — minimum 8 characters", example = "pass123456", required = true,
            minLength = 8)
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @Schema(description = "System role assigned to the user",
            example = "PLAYER", required = true,
            allowableValues = {"INVITED", "PLAYER"})
    @NotNull(message = "El rol del sistema es requerido")
    private Role role;

    @Schema(description = "User's full name as it will appear in the system",
            example = "Juan Pérez García", required = true)
    @NotBlank(message = "El nombre completo es requerido")
    private String fullName;

    @Schema(description = "Relationship to the university",
            example = "STUDENT", required = true,
            allowableValues = {"STUDENT", "TEACHER", "GRADUATE", "STAFF", "FAMILY"})
    @NotBlank(message = "La relación con la universidad es requerida")
    private String relationship;

    @Schema(description = "Academic program the user belongs to",
            example = "Ingeniería de Sistemas", required = true)
    @NotBlank(message = "El programa académico es requerido")
    private String program;

    @Schema(description = "Current semester (only required for STUDENT relationship)",
            example = "7", nullable = true)
    private Integer semester;

    @Schema(description = "Type of identification document",
            example = "CC", required = true,
            allowableValues = {"CC", "TI", "CE", "PP"})
    @NotBlank(message = "El tipo de documento es requerido")
    private String documentType;

    @Schema(description = "Identification document number",
            example = "12345678", required = true)
    @NotNull(message = "El número de documento es requerido")
    private Long documentNumber;

    private Long phone;

    @Schema(description = "User's date of birth — must be at least 16 years ago",
            example = "2000-01-01", required = true)
    @NotNull(message = "La fecha de nacimiento es requerida")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate birthDate;

    public RegisterRequest() {}

    // ── Getters & Setters ────────────────────────────────────────────

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public Long getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(Long documentNumber) { this.documentNumber = documentNumber; }

    public Long getPhone() { return phone; }
    public void setPhone(Long phone) { this.phone = phone; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
}
