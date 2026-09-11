package com.pos_backend.auth.infraestructure.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleBadJson() {
        return ResponseEntity.badRequest().body(Map.of("error", "Formato inválido en los datos enviados"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleNoAutorizado(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }


    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, String>> handleTokenExpirado() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "El token ha expirado"));
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Map<String, String>> handleTokenMalformado() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "El token tiene un formato inválido"));
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Map<String, String>> handleFirmaInvalida() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "La firma del token no es válida"));
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccesoDenegado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tienes permisos para realizar esta acción"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}