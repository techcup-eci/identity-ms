package com.escuelaing.techcup.service;

import com.escuelaing.techcup.model.AuditLog;
import com.escuelaing.techcup.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    @DisplayName("log guarda AuditLog con todos los campos correctos")
    void log_guardaCorrectamente() {
        auditService.log("LOGIN", "test@techcup.com", "Inicio de sesión", "127.0.0.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("LOGIN", saved.getAction());
        assertEquals("test@techcup.com", saved.getUserEmail());
        assertEquals("Inicio de sesión", saved.getDetails());
        assertEquals("127.0.0.1", saved.getIpAddress());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    @DisplayName("log guarda acción LOGOUT correctamente")
    void log_logout() {
        auditService.log("LOGOUT", "test@techcup.com", "Cierre de sesión", "192.168.1.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("LOGOUT", captor.getValue().getAction());
        assertEquals("192.168.1.1", captor.getValue().getIpAddress());
    }

    @Test
    @DisplayName("log guarda acción REGISTER correctamente")
    void log_register() {
        auditService.log("REGISTER", "nuevo@techcup.com", "Registro exitoso", "10.0.0.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("REGISTER", captor.getValue().getAction());
        assertEquals("nuevo@techcup.com", captor.getValue().getUserEmail());
    }

    @Test
    @DisplayName("log llama al repositorio exactamente una vez")
    void log_llamaRepositorioUnaVez() {
        auditService.log("CAMBIO_ROL", "admin@techcup.com", "Rol cambiado", "127.0.0.1");
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}