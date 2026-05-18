package com.escuelaing.techcup.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign client for users-and-players-ms.
 * Calls the /User endpoint directly (not through the orchestrator)
 * to create user profiles during registration.
 */
@FeignClient(name = "users-and-players-ms", url = "${services.user-service.url}")
public interface UserServiceClient {

    @PostMapping("/api/users")
    UserServiceResponse createUser(@RequestBody CreateUserRequest request);

    @PutMapping("/api/users/{id}/system-role")
    void updateSystemRole(@PathVariable("id") Long usersMsUserId, @RequestBody UpdateSystemRoleRequest request);

    /**
     * Request DTO for creating a user profile in users-and-players-ms.
     * Maps from identity-ms RegisterRequest.
     */
    class CreateUserRequest {
        private String name;
        private String email;
        private String birthDate;       // ISO date string (yyyy-MM-dd)
        private String systemRole;      // identity-ms role: PLAYER, CAPTAIN, ADMIN, etc.
        private String relationship;    // maps to relationship in users-ms UserDTO
        private String academicProgram;
        private Integer semester;
        private String identificationType;  // CC, TI, CE, PP
        private Long identificationNumber;
        private Long phone;             // default 0L

        public CreateUserRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getBirthDate() { return birthDate; }
        public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

        public String getSystemRole() { return systemRole; }
        public void setSystemRole(String systemRole) { this.systemRole = systemRole; }

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
    }

    /**
     * Response DTO returned by users-and-players-ms after user creation.
     * Contains the generated user ID from users-ms.
     */
    class UserServiceResponse {
        private Long id;
        private String name;
        private String email;
        private String birthDate;
        private String systemRole;
        private String relationship;
        private String academicProgram;
        private Integer semester;
        private String identificationType;
        private Long identificationNumber;
        private Long phone;

        public UserServiceResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getBirthDate() { return birthDate; }
        public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

        public String getSystemRole() { return systemRole; }
        public void setSystemRole(String systemRole) { this.systemRole = systemRole; }

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
    }

    /**
     * Request DTO for updating the system role in users-and-players-ms.
     */
    class UpdateSystemRoleRequest {
        private String systemRole;

        public UpdateSystemRoleRequest() {}

        public UpdateSystemRoleRequest(String systemRole) {
            this.systemRole = systemRole;
        }

        public String getSystemRole() { return systemRole; }
        public void setSystemRole(String systemRole) { this.systemRole = systemRole; }
    }
}
