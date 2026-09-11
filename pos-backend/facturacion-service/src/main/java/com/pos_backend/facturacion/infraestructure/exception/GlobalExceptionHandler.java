package com.pos_backend.facturacion.infraestructure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Sin esto, cualquier 400/500 (incluidos los que vienen de Factus, ej. "factura
    // pendiente" del sandbox compartido) solo se veía en la respuesta HTTP al frontend
    // — la consola del servicio quedaba muda. Con esto queda en la ventana de bootRun.
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        log.warn("400 Bad Request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400, "error", "Bad Request",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        log.warn("404 Not Found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 404, "error", "Not Found",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        // Acá sí con stacktrace completo — al usuario le mandamos un mensaje genérico
        // (podría ser cualquier cosa, hasta un bug nuestro), pero en la consola hace
        // falta ver la causa real para poder diagnosticar.
        log.error("500 Internal Server Error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 500, "error", "Internal Server Error",
                "message", "Ocurrió un error inesperado"
        ));
    }
}
