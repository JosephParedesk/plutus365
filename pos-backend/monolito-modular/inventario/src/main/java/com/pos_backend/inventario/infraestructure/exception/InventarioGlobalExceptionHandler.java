package com.pos_backend.inventario.infraestructure.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

// Clase renombrada de GlobalExceptionHandler (mismo motivo que
// AuthGlobalExceptionHandler: el nombre de bean por default sale del nombre de
// clase, y "GlobalExceptionHandler" ya lo usa el compartido).
//
// Este handler NO es el compartido: devuelve {"error": msg} en vez de
// {"timestamp","status","error","message"}. HALLAZGO (no se arregla acá, es un
// cambio de comportamiento aparte): el frontend de inventario en realidad lee
// error.response.data.message (el campo del handler COMPARTIDO, no el de
// este), así que hoy en el standalone los mensajes de validación específicos
// nunca le llegan al usuario — siempre cae al fallback genérico ("Error al
// guardar el producto"). Se preserva tal cual para no mezclar un fix de bug
// con la migración estructural.
@RestControllerAdvice(basePackages = "com.pos_backend.inventario.infraestructure.entry_points")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InventarioGlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadJson() {
        return ResponseEntity.badRequest().body(Map.of("error", "Formato inválido en los datos enviados"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
