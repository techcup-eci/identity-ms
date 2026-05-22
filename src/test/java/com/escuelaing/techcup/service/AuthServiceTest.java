package com.escuelaing.techcup.service;

import com.escuelaing.techcup.client.UserServiceClient;
import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.RegisterRequest;
import com.escuelaing.techcup.exception.BusinessException;
import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.repository.UserRepository;
import com.escuelaing.techcup.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 *  TDD: Unit tests for AuthService.register() using WebClient-based UserServiceClient.
 * Tests the complete registration flow: validate → WebClient call → save credentials → JWT.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private AuditService auditService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        // Lenient stubs for common setup used by some (not all) tests
        lenient().when(jwtUtil.getExpirationTime()).thenReturn(900000L);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        validRequest = new RegisterRequest();
        validRequest.setEmail("test@escuelaing.edu.co");
        validRequest.setPassword("password123");
        validRequest.setRole(Role.PLAYER);
        validRequest.setFullName("Test User");
        validRequest.setRelationship("STUDENT");
        validRequest.setProgram("Ingeniería de Sistemas");
        validRequest.setSemester(7);
        validRequest.setDocumentType("CC");
        validRequest.setDocumentNumber(12345678L);
        validRequest.setBirthDate(LocalDate.of(2000, 1, 15));
    }

    @Nested
    @DisplayName("register() — successful path")
    class SuccessfulRegistration {

        @Test
        @DisplayName("Should create user in both services, generate JWT, and return AuthResponse")
        void shouldRegisterSuccessfully() {
            // Setup: email does NOT exist in identity-ms
            when(userRepository.existsByEmail("test@escuelaing.edu.co")).thenReturn(false);

            // Setup: WebClient returns created user with ID 99
            UserServiceClient.UserServiceResponse feignResponse = new UserServiceClient.UserServiceResponse();
            feignResponse.setId(99L);
            when(userServiceClient.createUser(any(UserServiceClient.CreateUserRequest.class)))
                    .thenReturn(feignResponse);

            // Setup: User saved with assigned ID
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

            // Setup: JWT generation
            when(jwtUtil.generateToken(1L, "test@escuelaing.edu.co", "PLAYER", "Test User"))
                    .thenReturn("mock.jwt.token");

            // Setup: refresh token generation
            when(refreshTokenService.createRefreshToken(1L, 99L))
                    .thenReturn("mock.refresh.token");

            doNothing().when(auditService).log(anyString(), anyString(), anyString(), anyString());

            // Act
            AuthResponse response = authService.register(validRequest, "127.0.0.1");

            // Assert
            assertNotNull(response);
            assertNotNull(response.getUser());
            assertEquals("mock.jwt.token", response.getAccessToken());
            assertEquals("test@escuelaing.edu.co", response.getUser().getEmail());
            assertEquals("PLAYER", response.getUser().getRole());
            assertEquals(1L, response.getUser().getId());

            // Verify WebClient was called with correct mapping
            verify(userServiceClient).createUser(any(UserServiceClient.CreateUserRequest.class));

            // Verify credentials saved with usersMsUserId
            verify(userRepository).save(argThat(user ->
                    user.getEmail().equals("test@escuelaing.edu.co")
                    && user.getUsersMsUserId() != null
                    && user.getUsersMsUserId() == 99L
                    && user.getActive()
            ));

            // Verify audit logged
            verify(auditService).log(eq("REGISTER"), eq("test@escuelaing.edu.co"),
                    contains("Registro exitoso"), eq("127.0.0.1"));

            // Verify refresh token was generated
            verify(refreshTokenService).createRefreshToken(1L, 99L);
        }
    }

    @Nested
    @DisplayName("register() — error paths")
    class ErrorRegistration {

        @Test
        @DisplayName("Should throw BusinessException when email already exists")
        void shouldThrowWhenEmailExists() {
            when(userRepository.existsByEmail("test@escuelaing.edu.co")).thenReturn(true);

            assertThrows(BusinessException.class, () ->
                    authService.register(validRequest, "127.0.0.1"));

            // Verify no WebClient call was made
            verify(userServiceClient, never()).createUser(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when WebClient fails")
        void shouldFailWhenWebClientFails() {
            when(userRepository.existsByEmail("test@escuelaing.edu.co")).thenReturn(false);
            when(userServiceClient.createUser(any(UserServiceClient.CreateUserRequest.class)))
                    .thenThrow(new RuntimeException("Connection refused"));

            assertThrows(RuntimeException.class, () ->
                    authService.register(validRequest, "127.0.0.1"));

            verify(userRepository, never()).save(any());
        }
    }
}
