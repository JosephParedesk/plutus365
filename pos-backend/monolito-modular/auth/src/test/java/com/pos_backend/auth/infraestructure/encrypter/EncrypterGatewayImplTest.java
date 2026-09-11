package com.pos_backend.auth.infraestructure.encrypter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncrypterGatewayImplTest {

    private final EncrypterGatewayImpl encrypterGateway =
            new EncrypterGatewayImpl();

    @Test
    void deberiaEncriptarContrasena() {

        // Arrange
        String password = "123456";

        // Act
        String resultado = encrypterGateway.encrypt(password);

        // Assert
        assertNotNull(resultado);
        assertNotEquals(password, resultado);
    }

    @Test
    void deberiaValidarContrasenaCorrecta() {

        // Arrange
        String password = "123456";

        String encrypted =
                encrypterGateway.encrypt(password);

        // Act
        boolean resultado =
                encrypterGateway.matches(password, encrypted);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void deberiaRechazarContrasenaIncorrecta() {

        // Arrange
        String password = "123456";

        String encrypted =
                encrypterGateway.encrypt(password);

        // Act
        boolean resultado =
                encrypterGateway.matches("654321", encrypted);

        // Assert
        assertFalse(resultado);
    }
}