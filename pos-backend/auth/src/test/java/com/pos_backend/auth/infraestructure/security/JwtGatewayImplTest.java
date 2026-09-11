package com.pos_backend.auth.infraestructure.security;

import com.pos_backend.auth.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtGatewayImplTest {

    private JwtGatewayImpl jwtGateway;

    @BeforeEach
    void setUp() {
        jwtGateway = new JwtGatewayImpl();
        ReflectionTestUtils.setField(jwtGateway, "secret", "clave_secreta_super_segura_de_al_menos_32_bytes_larga");
        ReflectionTestUtils.setField(jwtGateway, "expirationMs", 3600000L);
    }

    @Test
    void generarToken_Exitoso_RetornaToken() {
        // Arrange
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "pass", "300", "admin", null, null);

        // Act
        String token = jwtGateway.generarToken(usuario);

        // Assert
        assertNotNull(token);
        assertFalse(token.trim().isEmpty());
    }

    @Test
    void extraerRol_Exitoso_RetornaRol() {
        // Arrange
        Usuario usuario = new Usuario("123456", "Juan", "juan@mail.com", "pass", "300", "admin", null, null);
        String token = jwtGateway.generarToken(usuario);

        // Act
        String rol = jwtGateway.extraerRol(token);

        // Assert
        assertNotNull(rol);
        assertEquals("admin", rol);
    }
}