package com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.auth.domain.model.Usuario;
import com.pos_backend.auth.infraestructure.mapper.UsuarioMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioDataGatewayImplTest {

    private UsuarioDataGatewayImpl usuarioDataGateway;
    private UsuarioDataJpaRepository usuarioDataJpaRepository;
    private UsuarioMapper usuarioMapper;

    @BeforeEach
    void setUp() {
        usuarioDataJpaRepository = mock(UsuarioDataJpaRepository.class);
        usuarioMapper = mock(UsuarioMapper.class);
        usuarioDataGateway = new UsuarioDataGatewayImpl(usuarioDataJpaRepository, usuarioMapper);
    }

    @Test
    void guardarUsuario_Exitoso_RetornaUsuario() {
        // Arrange
        Usuario usuario = new Usuario();
        UsuarioData usuarioData = new UsuarioData();
        when(usuarioMapper.tousuarioData(usuario)).thenReturn(usuarioData);
        when(usuarioDataJpaRepository.save(usuarioData)).thenReturn(usuarioData);
        when(usuarioMapper.toUsuario(usuarioData)).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioDataGateway.guardarUsuario(usuario);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado);
        verify(usuarioDataJpaRepository, times(1)).save(usuarioData);
    }

    @Test
    void buscarUsuarioPorCc_Existe_RetornaUsuario() {
        // Arrange
        String cedula = "123456";
        UsuarioData usuarioData = new UsuarioData();
        Usuario usuario = new Usuario();
        when(usuarioDataJpaRepository.findById(cedula)).thenReturn(Optional.of(usuarioData));
        when(usuarioMapper.toUsuario(usuarioData)).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioDataGateway.buscarUsuarioPorCc(cedula);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado);
    }

    @Test
    void buscarUsuarioPorCc_NoExiste_RetornaNull() {
        // Arrange
        String cedula = "123456";
        when(usuarioDataJpaRepository.findById(cedula)).thenReturn(Optional.empty());

        // Act
        Usuario resultado = usuarioDataGateway.buscarUsuarioPorCc(cedula);

        // Assert
        assertNull(resultado);
        verify(usuarioMapper, never()).toUsuario(any());
    }

    @Test
    void eliminarUsuarioPorCc_Exitoso_Elimina() {
        // Arrange
        String cedula = "123456";
        doNothing().when(usuarioDataJpaRepository).deleteById(cedula);

        // Act & Assert
        assertDoesNotThrow(() -> usuarioDataGateway.eliminarUsuarioPorCc(cedula));
        verify(usuarioDataJpaRepository, times(1)).deleteById(cedula);
    }

    @Test
    void buscarPorCorreo_Existe_RetornaUsuario() {
        // Arrange
        String correo = "juan@mail.com";
        UsuarioData usuarioData = new UsuarioData();
        Usuario usuario = new Usuario();
        when(usuarioDataJpaRepository.findByCorreo(correo)).thenReturn(Optional.of(usuarioData));
        when(usuarioMapper.toUsuario(usuarioData)).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioDataGateway.buscarPorCorreo(correo);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado);
    }

    @Test
    void buscarPorCorreo_NoExiste_RetornaNull() {
        // Arrange
        String correo = "juan@mail.com";
        when(usuarioDataJpaRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        // Act
        Usuario resultado = usuarioDataGateway.buscarPorCorreo(correo);

        // Assert
        assertNull(resultado);
    }

    @Test
    void buscarPorResetToken_Existe_RetornaUsuario() {
        // Arrange
        String token = "tokenXYZ";
        UsuarioData usuarioData = new UsuarioData();
        Usuario usuario = new Usuario();
        when(usuarioDataJpaRepository.findByResetPasswordToken(token)).thenReturn(Optional.of(usuarioData));
        when(usuarioMapper.toUsuario(usuarioData)).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioDataGateway.buscarPorResetToken(token);

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado);
    }

    @Test
    void buscarPorResetToken_NoExiste_RetornaNull() {
        // Arrange
        String token = "tokenXYZ";
        when(usuarioDataJpaRepository.findByResetPasswordToken(token)).thenReturn(Optional.empty());

        // Act
        Usuario resultado = usuarioDataGateway.buscarPorResetToken(token);

        // Assert
        assertNull(resultado);
    }
}