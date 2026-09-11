package com.pos_backend.auth.domain.usecase;

import com.pos_backend.auth.domain.model.Usuario;
import com.pos_backend.auth.domain.model.gateway.EncrypterGateway;
import com.pos_backend.auth.domain.model.gateway.JwtGateway;
import com.pos_backend.auth.domain.model.gateway.NotificationGateway;
import com.pos_backend.auth.domain.model.gateway.UsuarioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioUseCaseTest {

    private UsuarioUseCase usuarioUseCase;
    private UsuarioGateway usuarioGateway;
    private EncrypterGateway encrypterGateway;
    private JwtGateway jwtGateway;
    private NotificationGateway notificationGateway;

    @BeforeEach
    void setUp() {
        usuarioGateway = mock(UsuarioGateway.class);
        encrypterGateway = mock(EncrypterGateway.class);
        jwtGateway = mock(JwtGateway.class);
        notificationGateway = mock(NotificationGateway.class);
        usuarioUseCase = new UsuarioUseCase(usuarioGateway, encrypterGateway, jwtGateway, notificationGateway);
    }

    @Test
    void guardarUsuario_CamposVacios_LanzaException() {
        // Arrange
        Usuario usuario = new Usuario("", "Juan", "juan@mail.com", "123", "300", "user", null, null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.guardarUsuario(usuario));
    }

    @Test
    void guardarUsuario_SinRol_AsignaUserYGuarda() {
        // Arrange
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "password", "300", null, null, null);
        when(encrypterGateway.encrypt("password")).thenReturn("enc-pass");
        when(usuarioGateway.guardarUsuario(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Usuario resultado = usuarioUseCase.guardarUsuario(usuario);

        // Assert
        assertNotNull(resultado);
        assertEquals("user", resultado.getRol());
        verify(notificationGateway, times(1)).enviarNotificacion(resultado);
    }

    @Test
    void guardarUsuario_Exitoso_GuardaYNotifica() {
        // Arrange
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "password", "300", "admin", null, null);
        when(encrypterGateway.encrypt("password")).thenReturn("enc-pass");
        when(usuarioGateway.guardarUsuario(usuario)).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioUseCase.guardarUsuario(usuario);

        // Assert
        assertNotNull(resultado);
        assertEquals("admin", resultado.getRol());
        verify(notificationGateway, times(1)).enviarNotificacion(usuario);
    }

    @Test
    void buscarUsuarioPorCc_Exitoso_RetornaUsuario() {
        // Arrange
        String cedula = "123456";
        Usuario usuario = new Usuario();
        when(usuarioGateway.buscarUsuarioPorCc(cedula)).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioUseCase.buscarUsuarioPorCc(cedula);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado);
    }

    @Test
    void buscarUsuarioPorCc_NoExiste_LanzaException() {
        // Arrange
        String cedula = "123456";
        when(usuarioGateway.buscarUsuarioPorCc(cedula)).thenReturn(null);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> usuarioUseCase.buscarUsuarioPorCc(cedula));
    }

    @Test
    void eliminarUsuarioPorCc_Exitoso_Elimina() {
        // Arrange
        String cedula = "123456";
        Usuario usuario = new Usuario();
        when(usuarioGateway.buscarUsuarioPorCc(cedula)).thenReturn(usuario);
        doNothing().when(usuarioGateway).eliminarUsuarioPorCc(cedula);

        // Act
        usuarioUseCase.eliminarUsuarioPorCc(cedula);

        // Assert
        verify(usuarioGateway, times(1)).eliminarUsuarioPorCc(cedula);
    }

    @Test
    void eliminarUsuarioPorCc_NoExiste_LanzaException() {
        // Arrange
        String cedula = "123456";
        when(usuarioGateway.buscarUsuarioPorCc(cedula)).thenReturn(null);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> usuarioUseCase.eliminarUsuarioPorCc(cedula));
    }

    @Test
    void login_ArgumentosNulos_LanzaException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.login(null, "password"));
    }

    @Test
    void login_CorreoInvalido_LanzaException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.login("correoInvalido", "password"));
    }

    @Test
    void login_UsuarioNoExiste_LanzaException() {
        // Arrange
        String email = "juan@mail.com";
        when(usuarioGateway.buscarPorCorreo(email)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.login(email, "password"));
    }

    @Test
    void login_UsuarioSinCedula_LanzaException() {
        // Arrange
        String email = "juan@mail.com";
        Usuario usuario = new Usuario(null, "Juan", email, "pass", "300", "user", null, null);
        when(usuarioGateway.buscarPorCorreo(email)).thenReturn(usuario);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.login(email, "password"));
    }

    @Test
    void login_ContrasenaNulaEnBd_LanzaException() {
        // Arrange
        String email = "juan@mail.com";
        Usuario usuario = new Usuario("123456", "Juan", email, null, "300", "user", null, null);
        when(usuarioGateway.buscarPorCorreo(email)).thenReturn(usuario);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.login(email, "password"));
    }

    @Test
    void login_ContrasenaIncorrecta_LanzaException() {
        // Arrange
        String email = "juan@mail.com";
        Usuario usuario = new Usuario("123456", "Juan", email, "enc-pass", "300", "user", null, null);
        when(usuarioGateway.buscarPorCorreo(email)).thenReturn(usuario);
        when(encrypterGateway.matches("password", "enc-pass")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.login(email, "password"));
    }

    @Test
    void login_Exitoso_RetornaToken() {
        // Arrange
        String email = "juan@mail.com";
        Usuario usuario = new Usuario("123456", "Juan", email, "enc-pass", "300", "user", null, null);
        when(usuarioGateway.buscarPorCorreo(email)).thenReturn(usuario);
        when(encrypterGateway.matches("password", "enc-pass")).thenReturn(true);
        when(jwtGateway.generarToken(usuario)).thenReturn("jwt-token");

        // Act
        String token = usuarioUseCase.login(email, "password");

        // Assert
        assertEquals("jwt-token", token);
    }

    @Test
    void forgotPassword_EmailVacio_LanzaException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.forgotPassword(" "));
    }

    @Test
    void forgotPassword_UsuarioNoExiste_LanzaException() {
        // Arrange
        String email = "juan@mail.com";
        when(usuarioGateway.buscarPorCorreo(email)).thenReturn(null);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> usuarioUseCase.forgotPassword(email));
    }

    @Test
    void forgotPassword_Exitoso_EnviaNotificacion() {
        // Arrange
        String email = "juan@mail.com";
        Usuario usuario = new Usuario("123456", "Juan", email, "pass", "300", "user", null, null);
        when(usuarioGateway.buscarPorCorreo(email)).thenReturn(usuario);

        // Act
        usuarioUseCase.forgotPassword(email);

        // Assert
        assertNotNull(usuario.getResetPasswordToken());
        assertNotNull(usuario.getResetPasswordTokenExpiry());
        verify(usuarioGateway, times(1)).guardarUsuario(usuario);
        verify(notificationGateway, times(1)).enviarNotificacionRecuperacion(usuario);
    }

    @Test
    void resetPassword_TokenVacio_LanzaException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.resetPassword("", "nuevaPass"));
    }

    @Test
    void resetPassword_ContrasenaVacia_LanzaException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.resetPassword("token", " "));
    }

    @Test
    void resetPassword_TokenInvalido_LanzaException() {
        // Arrange
        String token = "tokenXYZ";
        when(usuarioGateway.buscarPorResetToken(token)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.resetPassword(token, "nuevaPass"));
    }

    @Test
    void resetPassword_ExpiracionNula_LanzaException() {
        // Arrange
        String token = "tokenXYZ";
        Usuario usuario = new Usuario("123456", "Juan", "j@m.com", "pass", "300", "user", token, null);
        when(usuarioGateway.buscarPorResetToken(token)).thenReturn(usuario);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.resetPassword(token, "nuevaPass"));
    }

    @Test
    void resetPassword_TokenExpirado_LanzaException() {
        // Arrange
        String token = "tokenXYZ";
        Usuario usuario = new Usuario("123456", "Juan", "j@m.com", "pass", "300", "user", token, LocalDateTime.now().minusMinutes(5));
        when(usuarioGateway.buscarPorResetToken(token)).thenReturn(usuario);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioUseCase.resetPassword(token, "nuevaPass"));
    }

    @Test
    void resetPassword_Exitoso_ActualizaContrasena() {
        // Arrange
        String token = "tokenXYZ";
        Usuario usuario = new Usuario("123456", "Juan", "j@m.com", "pass", "300", "user", token, LocalDateTime.now().plusMinutes(5));
        when(usuarioGateway.buscarPorResetToken(token)).thenReturn(usuario);
        when(encrypterGateway.encrypt("nuevaPass")).thenReturn("enc-nuevaPass");

        // Act
        usuarioUseCase.resetPassword(token, "nuevaPass");

        // Assert
        assertEquals("enc-nuevaPass", usuario.getContrasena());
        assertNull(usuario.getResetPasswordToken());
        assertNull(usuario.getResetPasswordTokenExpiry());
        verify(usuarioGateway, times(1)).guardarUsuario(usuario);
    }
}