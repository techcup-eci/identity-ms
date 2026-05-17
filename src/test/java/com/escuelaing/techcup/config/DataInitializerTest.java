package com.escuelaing.techcup.config;

import com.escuelaing.techcup.model.Role;
import com.escuelaing.techcup.model.User;
import com.escuelaing.techcup.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dataInitializer, "adminDefaultPassword", "admin123");
    }

    @Test
    @DisplayName("Crea admin si no existe")
    void run_creaAdminSiNoExiste() throws Exception {
        when(userRepository.existsByEmail("admin@techcup.com")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded_admin123");

        dataInitializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User admin = captor.getValue();
        assertEquals("admin@techcup.com", admin.getEmail());
        assertEquals("encoded_admin123", admin.getPassword());
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.getActive());
    }

    @Test
    @DisplayName("No crea admin si ya existe")
    void run_noCreaSiYaExiste() throws Exception {
        when(userRepository.existsByEmail("admin@techcup.com")).thenReturn(true);

        dataInitializer.run();

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}