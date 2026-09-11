package com.pos_backend.auth.infraestructure.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleBadJson_RetornaBadRequest() {
        // Arrange

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleBadJson();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Formato inválido en los datos enviados", response.getBody().get("error"));
    }

    @Test
    void handleNotFound_RetornaNotFound() {
        // Arrange
        NoSuchElementException ex = new NoSuchElementException("Usuario no encontrado");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleNotFound(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Usuario no encontrado", response.getBody().get("error"));
    }

    @Test
    void handleNoAutorizado_RetornaUnauthorized() {
        // Arrange
        SecurityException ex = new SecurityException("No autorizado");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleNoAutorizado(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("No autorizado", response.getBody().get("error"));
    }

    @Test
    void handleTokenExpirado_RetornaUnauthorized() {
        // Arrange

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleTokenExpirado();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("El token ha expirado", response.getBody().get("error"));
    }

    @Test
    void handleTokenMalformado_RetornaUnauthorized() {
        // Arrange

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleTokenMalformado();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("El token tiene un formato inválido", response.getBody().get("error"));
    }

    @Test
    void handleFirmaInvalida_RetornaUnauthorized() {
        // Arrange

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleFirmaInvalida();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("La firma del token no es válida", response.getBody().get("error"));
    }

    @Test
    void handleAccesoDenegado_RetornaForbidden() {
        // Arrange

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccesoDenegado();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("No tienes permisos para realizar esta acción", response.getBody().get("error"));
    }

    @Test
    void handleRuntime_RetornaBadRequest() {
        // Arrange
        RuntimeException ex = new RuntimeException("Error en el servidor");

        // Act
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntime(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error en el servidor", response.getBody().get("error"));
    }
}