package com.escuelaing.techcup.client;

import com.escuelaing.techcup.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;

@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    @Autowired
    private WebClient webClient;

    // ── Crear usuario ────────────────────────────────────────────────

    public UserServiceResponse createUser(CreateUserRequest request) {
        try {
            return webClient.post()
                    .uri("/api/users/register")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(UserServiceResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error al crear usuario en users-ms: status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("Error al registrar el usuario: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error de conexión con users-ms: {}", e.getMessage());
            throw new BusinessException("No se pudo conectar con el servicio de usuarios. Intente más tarde.");
        }
    }

    // ── Actualizar rol ───────────────────────────────────────────────

    public void updateSystemRole(Long usersMsUserId, UpdateSystemRoleRequest request) {
        try {
            webClient.patch()
                    .uri("/api/users/{id}/system-role", usersMsUserId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.warn("No se pudo sincronizar el rol en users-ms para userId={}: {}", usersMsUserId, e.getMessage());
        }
    }

    // ── Verificar inscripción activa ─────────────────────────────────

    public Boolean hasActiveEnrollment(Long usersMsUserId) {
        try {
            return webClient.get()
                    .uri("/api/users/{id}/has-active-enrollment", usersMsUserId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (Exception e) {
            log.warn("No se pudo verificar inscripción activa para userId={}: {}", usersMsUserId, e.getMessage());
            throw new RuntimeException("No se pudo verificar el estado del torneo", e);
        }
    }

    // ── Request ──────────────────────────────────────────────────────

    public static class CreateUserRequest {
        private String name;
        private String email;
        private LocalDate birthDate;
        @com.fasterxml.jackson.annotation.JsonProperty("schoolRelation")
        private String relationship;
        private String academicProgram;
        private Integer semester;
        private String identificationType;
        private Long identificationNumber;
        private Long phone;
        private String password;
        private String academicLevel;
        private String professorType;

        public CreateUserRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
        public String getAcademicProgram() { return academicProgram; }
        public void setAcademicProgram(String academicProgram) { this.academicProgram = academicProgram; }
        public Integer getSemester() { return semester; }
        public void setSemester(Integer semester) { this.semester = semester; }
        public String getIdentificationType() { return identificationType; }
        public void setIdentificationType(String identificationType) { this.identificationType = identificationType; }
        public Long getIdentificationNumber() { return identificationNumber; }
        public void setIdentificationNumber(Long identificationNumber) { this.identificationNumber = identificationNumber; }
        public Long getPhone() { return phone; }
        public void setPhone(Long phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getAcademicLevel() { return academicLevel; }
        public void setAcademicLevel(String academicLevel) { this.academicLevel = academicLevel; }
        public String getProfessorType() { return professorType; }
        public void setProfessorType(String professorType) { this.professorType = professorType; }
    }

    // ── Response — solo necesitamos el id ────────────────────────────

    public static class UserServiceResponse {
        private Long id;

        public UserServiceResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    // ── Role update request ──────────────────────────────────────────

    public static class UpdateSystemRoleRequest {
        private String role;

        public UpdateSystemRoleRequest() {}
        public UpdateSystemRoleRequest(String role) { this.role = role; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
