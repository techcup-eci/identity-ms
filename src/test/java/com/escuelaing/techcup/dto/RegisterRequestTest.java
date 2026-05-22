package com.escuelaing.techcup.dto;

import com.escuelaing.techcup.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD: Tests for expanded RegisterRequest with all registration fields.
 */
class RegisterRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("All fields should be gettable and settable")
    void shouldHaveAllFieldsAccessible() {
        RegisterRequest request = new RegisterRequest();
        LocalDate birthDate = LocalDate.of(2000, 1, 15);

        request.setEmail("test@escuelaing.edu.co");
        request.setPassword("password123");
        request.setRole(Role.PLAYER);
        request.setFullName("Test User");
        request.setRelationship("STUDENT");
        request.setProgram("Ingeniería de Sistemas");
        request.setSemester(7);
        request.setDocumentType("CC");
        request.setDocumentNumber(12345678L);
        request.setBirthDate(birthDate);

        assertEquals("test@escuelaing.edu.co", request.getEmail());
        assertEquals("password123", request.getPassword());
        assertEquals(Role.PLAYER, request.getRole());
        assertEquals("Test User", request.getFullName());
        assertEquals("STUDENT", request.getRelationship());
        assertEquals("Ingeniería de Sistemas", request.getProgram());
        assertEquals(7, request.getSemester());
        assertEquals("CC", request.getDocumentType());
        assertEquals(12345678L, request.getDocumentNumber());
        assertEquals(birthDate, request.getBirthDate());
    }

    @Test
    @DisplayName("Valid registration request should pass validation")
    void validRequestShouldPassValidation() {
        RegisterRequest request = buildValidRequest();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    @DisplayName("Missing email should fail validation")
    void missingEmailShouldFailValidation() {
        RegisterRequest request = buildValidRequest();
        request.setEmail(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Should have violations for missing email");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")),
                "Violation should be on email field");
    }

    @Test
    @DisplayName("Short password should fail validation")
    void shortPasswordShouldFailValidation() {
        RegisterRequest request = buildValidRequest();
        request.setPassword("short");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Should have violations for short password");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")),
                "Violation should be on password field");
    }

    @Test
    @DisplayName("Missing fullName should fail validation")
    void missingFullNameShouldFailValidation() {
        RegisterRequest request = buildValidRequest();
        request.setFullName(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Should have violations for missing fullName");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fullName")),
                "Violation should be on fullName field");
    }

    @Test
    @DisplayName("Missing program should fail validation")
    void missingProgramShouldFailValidation() {
        RegisterRequest request = buildValidRequest();
        request.setProgram(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Should have violations for missing program");
    }

    @Test
    @DisplayName("Missing birthDate should fail validation")
    void missingBirthDateShouldFailValidation() {
        RegisterRequest request = buildValidRequest();
        request.setBirthDate(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Should have violations for missing birthDate");
    }

    @Test
    @DisplayName("Future birthDate should fail validation")
    void futureBirthDateShouldFailValidation() {
        RegisterRequest request = buildValidRequest();
        request.setBirthDate(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Should have violations for future birthDate");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("birthDate")),
                "Violation should be on birthDate field");
    }

    @Test
    @DisplayName("semester should be optional (null allowed)")
    void semesterShouldBeOptional() {
        RegisterRequest request = buildValidRequest();
        request.setSemester(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "Null semester should be valid");
    }

    @Nested
    @DisplayName("Triangulation: different role values")
    class RoleValidation {

        @Test
        @DisplayName("CAPTAIN role should be valid")
        void captainRoleShouldBeValid() {
            RegisterRequest request = buildValidRequest();
            request.setRole(Role.CAPTAIN);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertTrue(violations.isEmpty(), "CAPTAIN role should be valid");
        }

        @Test
        @DisplayName("ADMIN role should be valid")
        void adminRoleShouldBeValid() {
            RegisterRequest request = buildValidRequest();
            request.setRole(Role.ADMIN);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertTrue(violations.isEmpty(), "ADMIN role should be valid");
        }

        @Test
        @DisplayName("Null role should fail validation")
        void nullRoleShouldFailValidation() {
            RegisterRequest request = buildValidRequest();
            request.setRole(null);

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertFalse(violations.isEmpty(), "Null role should fail validation");
        }
    }

    @Nested
    @DisplayName("Triangulation: different relationship values")
    class RelationshipValidation {

        @Test
        @DisplayName("TEACHER relationship should be valid")
        void teacherRelationshipShouldBeValid() {
            RegisterRequest request = buildValidRequest();
            request.setRelationship("TEACHER");

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertTrue(violations.isEmpty(), "TEACHER relationship should be valid");
        }

        @Test
        @DisplayName("FAMILY relationship should be valid")
        void familyRelationshipShouldBeValid() {
            RegisterRequest request = buildValidRequest();
            request.setRelationship("FAMILY");

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

            assertTrue(violations.isEmpty(), "FAMILY relationship should be valid");
        }
    }

    private RegisterRequest buildValidRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@escuelaing.edu.co");
        request.setPassword("password123");
        request.setRole(Role.PLAYER);
        request.setFullName("Test User");
        request.setRelationship("STUDENT");
        request.setProgram("Ingeniería de Sistemas");
        request.setSemester(7);
        request.setDocumentType("CC");
        request.setDocumentNumber(12345678L);
        request.setBirthDate(LocalDate.of(2000, 1, 15));
        return request;
    }
}
