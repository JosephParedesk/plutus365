package com.pos_backend.auth.infraestructure.mapper;

import com.pos_backend.auth.domain.model.Usuario;
import com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository.UsuarioData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    private UsuarioMapper usuarioMapper;

    @BeforeEach
    void setUp() {
        usuarioMapper = new UsuarioMapper();
    }

    @Test
    void tousuarioData_ShouldMapUsuarioToUsuarioData() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.now();
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "password123", "3001234567", "ADMIN", "tokenXYZ", fecha);

        // Act
        UsuarioData resultado = usuarioMapper.tousuarioData(usuario);

        // Assert
        assertNotNull(resultado);
        assertEquals("123456", resultado.getCedula());
        assertEquals("Juan", resultado.getNombre());
        assertEquals("juan@mail.com", resultado.getCorreo());
        assertEquals("password123", resultado.getContrasena());
        assertEquals("3001234567", resultado.getTelefono());
        assertEquals("ADMIN", resultado.getRol());
        assertEquals("tokenXYZ", resultado.getResetPasswordToken());
        assertEquals(fecha, resultado.getResetPasswordTokenExpiry());
    }

    @Test
    void toUsuario_ShouldMapUsuarioDataToUsuario() {
        // Arrange
        LocalDateTime fecha = LocalDateTime.now();
        UsuarioData usuarioData = new UsuarioData("123456", "Juan", "juan@mail.com", "password123", "3001234567", "ADMIN", "tokenXYZ", fecha);

        // Act
        Usuario resultado = usuarioMapper.toUsuario(usuarioData);

        // Assert
        assertNotNull(resultado);
        assertEquals("123456", resultado.getCedula());
        assertEquals("Juan", resultado.getNombre());
        assertEquals("juan@mail.com", resultado.getCorreo());
        assertEquals("password123", resultado.getContrasena());
        assertEquals("3001234567", resultado.getTelefono());
        assertEquals("ADMIN", resultado.getRol());
        assertEquals("tokenXYZ", resultado.getResetPasswordToken());
        assertEquals(fecha, resultado.getResetPasswordTokenExpiry());
    }
}