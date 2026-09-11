package com.pos_backend.inventario.infraestructure.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleBadJson_Exitoso_RetornaBadRequest() {
        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleBadJson();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Formato inválido en los datos enviados", response.getBody().get("error"));
    }

    @Test
    void handleNotFound_Exitoso_RetornaNotFound() {
        // Arrange
        String mensajeError = "Elemento no encontrado en la base de datos";
        NoSuchElementException excepcion = new NoSuchElementException(mensajeError);

        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleNotFound(excepcion);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mensajeError, response.getBody().get("error"));
    }

    @Test
    void handleRuntime_Exitoso_RetornaBadRequest() {
        // Arrange
        String mensajeError = "Error interno en el servidor o regla de negocio rota";
        RuntimeException excepcion = new RuntimeException(mensajeError);

        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleRuntime(excepcion);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mensajeError, response.getBody().get("error"));
    }
}