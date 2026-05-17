package com.escuelaing.techcup.security;

import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityTest {

    // ─────────────────────────────────────────────
    // InternalRequestFilter
    // ─────────────────────────────────────────────

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private InternalRequestFilter internalRequestFilter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(internalRequestFilter, "internalSecret", "mi-secret-interno");
    }

    @Test
    @DisplayName("Filter permite /api/identity/login sin secret")
    void filter_permitLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        internalRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Filter permite /api/identity/register sin secret")
    void filter_permitRegister() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/register");
        MockHttpServletResponse response = new MockHttpServletResponse();

        internalRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Filter permite acceso con secret correcto")
    void filter_secretCorrecto() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/refresh-token");
        request.addHeader("X-Internal-Secret", "mi-secret-interno");
        MockHttpServletResponse response = new MockHttpServletResponse();

        internalRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Filter bloquea acceso sin secret")
    void filter_sinSecret() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/refresh-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        internalRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    @DisplayName("Filter bloquea acceso con secret incorrecto")
    void filter_secretIncorrecto() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/identity/refresh-token");
        request.addHeader("X-Internal-Secret", "secret-malo");
        MockHttpServletResponse response = new MockHttpServletResponse();

        internalRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    // ─────────────────────────────────────────────
    // UserDetailsServiceImpl
    // ─────────────────────────────────────────────

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("loadUserByUsername retorna UserDetails cuando el usuario existe")
    void loadUserByUsername_exitoso() {
        User user = new User();
        user.setEmail("test@techcup.com");
        user.setPassword("encoded_password");
        user.setRole(Role.PLAYER);

        when(userRepository.findByEmail("test@techcup.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("test@techcup.com");

        assertNotNull(details);
        assertEquals("test@techcup.com", details.getUsername());
        assertEquals("encoded_password", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PLAYER")));
    }

    @Test
    @DisplayName("loadUserByUsername lanza UsernameNotFoundException cuando no existe")
    void loadUserByUsername_noExiste() {
        when(userRepository.findByEmail("noexiste@techcup.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("noexiste@techcup.com"));
    }
}