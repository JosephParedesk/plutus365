package com.pos_backend.auth.infraestructure.notification;

import com.pos_backend.auth.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationGatewayImplTest {

    private NotificationGatewayImpl notificationGateway;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        notificationGateway = new NotificationGatewayImpl(restTemplate);
        ReflectionTestUtils.setField(notificationGateway, "notificationServiceUrl", "http://localhost:9092/api/notification/send");
    }

    @Test
    void enviarNotificacion_ConTelefono_Exitoso() {
        // Arrange
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "pass", "3001234567", "user", null, null);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn("OK");

        // Act & Assert
        assertDoesNotThrow(() -> notificationGateway.enviarNotificacion(usuario));
        verify(restTemplate, times(1)).postForObject(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void enviarNotificacion_SinTelefono_Exitoso() {
        // Arrange
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "pass", null, "user", null, null);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn("OK");

        // Act & Assert
        assertDoesNotThrow(() -> notificationGateway.enviarNotificacion(usuario));
        verify(restTemplate, times(1)).postForObject(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void enviarNotificacion_Error_CapturaExcepcion() {
        // Arrange
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "pass", "3001234567", "user", null, null);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenThrow(new RuntimeException("Error de red"));

        // Act & Assert
        assertDoesNotThrow(() -> notificationGateway.enviarNotificacion(usuario));
    }

    @Test
    void enviarNotificacionRecuperacion_ConTelefono_Exitoso() {
        // Arrange
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "pass", "3001234567", "user", "tokenXYZ", null);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn("OK");

        // Act & Assert
        assertDoesNotThrow(() -> notificationGateway.enviarNotificacionRecuperacion(usuario));
        verify(restTemplate, times(1)).postForObject(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void enviarNotificacionRecuperacion_SinTelefono_Exitoso() {
        // Arrange
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "pass", null, "user", "tokenXYZ", null);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenReturn("OK");

        // Act & Assert
        assertDoesNotThrow(() -> notificationGateway.enviarNotificacionRecuperacion(usuario));
        verify(restTemplate, times(1)).postForObject(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void enviarNotificacionRecuperacion_Error_LanzaExcepcion() {
        // Arrange
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "pass", "3001234567", "user", "tokenXYZ", null);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class))).thenThrow(new RuntimeException("Error de red"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> notificationGateway.enviarNotificacionRecuperacion(usuario));
    }
}