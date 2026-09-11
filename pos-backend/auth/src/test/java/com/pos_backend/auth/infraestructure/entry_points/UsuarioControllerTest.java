package com.pos_backend.auth.infraestructure.entry_points;

import com.pos_backend.auth.domain.model.Usuario;
import com.pos_backend.auth.domain.model.gateway.JwtGateway;
import com.pos_backend.auth.domain.usecase.UsuarioUseCase;
import com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository.UsuarioData;
import com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository.dto.ForgotPasswordRequest;
import com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository.dto.ResetPasswordRequest;
import com.pos_backend.auth.infraestructure.mapper.UsuarioMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioControllerTest {

    private UsuarioController usuarioController;
    private UsuarioUseCase usuarioUseCase;
    private UsuarioMapper usuarioMapper;
    private JwtGateway jwtGateway;

    @BeforeEach
    void setUp() {
        usuarioUseCase = mock(UsuarioUseCase.class);
        usuarioMapper = mock(UsuarioMapper.class);
        jwtGateway = mock(JwtGateway.class);
        usuarioController = new UsuarioController(usuarioUseCase, usuarioMapper, jwtGateway);
    }

    @Test
    void saveUsauraio_Exitoso_RetornaUsuario() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.now();
        UsuarioData usuarioData = new UsuarioData("123456", "Juan", "juan@mail.com", "password123", "3001234567", "ADMIN", "tokenXYZ", fecha);
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "password123", "3001234567", "ADMIN", "tokenXYZ", fecha);
        when(usuarioMapper.toUsuario(usuarioData)).thenReturn(usuario);
        when(usuarioUseCase.guardarUsuario(usuario)).thenReturn(usuario);

        // Act
        ResponseEntity<Usuario> response = usuarioController.saveUsauraio(usuarioData);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(usuario, response.getBody());
    }

    @Test
    void buscarUsuario_Exitoso_RetornaUsuario() {
        // Arrange
        String cedula = "123456";
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "password123", "3001234567", "ADMIN", "tokenXYZ", null);
        when(usuarioUseCase.buscarUsuarioPorCc(cedula)).thenReturn(usuario);

        // Act
        ResponseEntity<Usuario> response = usuarioController.buscarUsuario(cedula);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(usuario, response.getBody());
    }

    @Test
    void eliminarUsuario_NoAdmin_RetornaForbidden() {
        // Arrange
        String cedula = "123456";
        String authHeader = "Bearer token-usuario";
        when(jwtGateway.extraerRol("token-usuario")).thenReturn("USER");

        // Act
        ResponseEntity<Void> response = usuarioController.eliminarUsuario(cedula, authHeader);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(usuarioUseCase, never()).eliminarUsuarioPorCc(anyString());
    }

    @Test
    void eliminarUsuario_Admin_RetornaNoContent() {
        // Arrange
        String cedula = "123456";
        String authHeader = "Bearer token-admin";
        when(jwtGateway.extraerRol("token-admin")).thenReturn("admin");
        doNothing().when(usuarioUseCase).eliminarUsuarioPorCc(cedula);

        // Act
        ResponseEntity<Void> response = usuarioController.eliminarUsuario(cedula, authHeader);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(jwtGateway, times(1)).extraerRol("token-admin");
        verify(usuarioUseCase, times(1)).eliminarUsuarioPorCc(cedula);
    }

    @Test
    void login_Exitoso_RetornaToken() {
        // Arrange
        UsuarioData usuarioData = mock(UsuarioData.class);
        when(usuarioData.getCorreo()).thenReturn("juan@mail.com");
        when(usuarioData.getContrasena()).thenReturn("password123");
        when(usuarioUseCase.login("juan@mail.com", "password123")).thenReturn("token-jwt-exitoso");

        // Act
        ResponseEntity<String> response = usuarioController.login(usuarioData);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token-jwt-exitoso", response.getBody());
    }

    @Test
    void forgotPassword_Exitoso_RetornaMensaje() {
        // Arrange
        ForgotPasswordRequest request = mock(ForgotPasswordRequest.class);
        when(request.getEmail()).thenReturn("juan@mail.com");
        doNothing().when(usuarioUseCase).forgotPassword("juan@mail.com");

        // Act
        ResponseEntity<String> response = usuarioController.forgotPassword(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Se ha enviado un token de recuperación al correo registrado", response.getBody());
    }

    @Test
    void resetPassword_Exitoso_RetornaMensaje() {
        // Arrange
        ResetPasswordRequest request = mock(ResetPasswordRequest.class);
        when(request.getToken()).thenReturn("tokenXYZ");
        when(request.getNuevaContrasena()).thenReturn("nuevaPassword123");
        doNothing().when(usuarioUseCase).resetPassword("tokenXYZ", "nuevaPassword123");

        // Act
        ResponseEntity<String> response = usuarioController.resetPassword(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Contraseña actualizada exitosamente", response.getBody());
    }
}