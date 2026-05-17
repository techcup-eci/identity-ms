package com.escuelaing.techcup.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    @DisplayName("BusinessException con mensaje lo conserva correctamente")
    void constructor_conMensaje() {
        BusinessException ex = new BusinessException("Error de negocio");
        assertEquals("Error de negocio", ex.getMessage());
    }

    @Test
    @DisplayName("BusinessException con mensaje y causa los conserva correctamente")
    void constructor_conMensajeYCausa() {
        Throwable causa = new RuntimeException("causa raíz");
        BusinessException ex = new BusinessException("Error de negocio", causa);
        assertEquals("Error de negocio", ex.getMessage());
        assertEquals(causa, ex.getCause());
    }

    @Test
    @DisplayName("BusinessException es instancia de RuntimeException")
    void esRuntimeException() {
        BusinessException ex = new BusinessException("error");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("BusinessException se puede lanzar y capturar")
    void sePuedeLanzarYCapturar() {
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException("lanzada");
        });
    }
}