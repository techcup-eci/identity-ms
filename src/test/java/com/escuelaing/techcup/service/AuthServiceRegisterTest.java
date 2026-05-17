package com.escuelaing.techcup.service;

import com.escuelaing.techcup.dto.AuthResponse;
import com.escuelaing.techcup.dto.RegisterRequest;
import com.escuelaing.techcup.dto.UserServiceResponse;
import com.escuelaing.techcup.exception.BusinessException;
import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.repository.UserRepository;
import com.escuelaing.techcup.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditService auditService;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "apiGatewayUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "internalSecret", "mi-secret");
    }

    @SuppressWarnings("unchecked")
    private void mockWebClient(UserServiceResponse userResponse) {
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserServiceResponse.class)).thenReturn(Mono.just(userResponse));
    }

    @Test
    @DisplayName("register exitoso crea credenciales y retorna AuthResponse")
    void register_exitoso() {
        UserServiceResponse userResponse = new UserServiceResponse();
        userResponse.setId(1L);
        userResponse.setEmail("nuevo@techcup.com");
        userResponse.setRol("PLAYER");

        mockWebClient(userResponse);

        when(userRepository.existsByEmail("nuevo@techcup.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken("nuevo@techcup.com", "PLAYER")).thenReturn("jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(36000000L);
        doNothing().when(auditService).log(any(), any(), any(), any());

        RegisterRequest request = new RegisterRequest();
        request.setEmail("nuevo@techcup.com");
        request.setPassword("password123");
        request.setRole(Role.PLAYER);

        AuthResponse response = authService.register(request, "127.0.0.1");

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("nuevo@techcup.com", response.getEmail());
        assertEquals("PLAYER", response.getRole());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
        verify(auditService).log(eq("REGISTER"), eq("nuevo@techcup.com"), any(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("register falla si el correo ya está registrado")
    void register_correoYaRegistrado() {
        UserServiceResponse userResponse = new UserServiceResponse();
        userResponse.setId(1L);
        userResponse.setEmail("existente@techcup.com");
        userResponse.setRol("PLAYER");

        mockWebClient(userResponse);

        when(userRepository.existsByEmail("existente@techcup.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("existente@techcup.com");
        request.setPassword("password123");
        request.setRole(Role.PLAYER);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(request, "127.0.0.1"));

        assertEquals("El correo ya está registrado", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}