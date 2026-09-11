package com.pos_backend.auth.infraestructure.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.NoSuchElementException;

// Clase renombrada de GlobalExceptionHandler a AuthGlobalExceptionHandler
// (única diferencia con el original — mismo contenido, mismos métodos, mismas
// respuestas). Necesario porque el nombre de bean por default de Spring sale
// del nombre de la clase, y "GlobalExceptionHandler" ya lo usa el handler
// compartido de app/infraestructure/exception/ — @RestControllerAdvice no
// tiene un atributo separado para fijar el nombre de bean (su "value" es
// alias de basePackages, no del id del bean, a diferencia de @Component).
//
// Este handler NO es el genérico compartido (a diferencia de
// categoria/proveedor/cliente/subscription, el de auth devuelve un formato
// distinto — {"error": msg} en vez de {"timestamp", "status", "error",
// "message"} — y agrega manejo específico de JWT). Se mantiene aparte y con
// scope a las rutas de auth (basePackages) + prioridad explícita (@Order)
// para que gane sobre el genérico en los @ExceptionHandler que se pisan
// (RuntimeException, NoSuchElementException) sin volverse ambiguo. Fuera de
// com.pos_backend.auth.infraestructure.entry_points sigue aplicando el
// compartido, igual que antes de esta migración.
@RestControllerAdvice(basePackages = "com.pos_backend.auth.infraestructure.entry_points")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthGlobalExceptionHandler {

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
