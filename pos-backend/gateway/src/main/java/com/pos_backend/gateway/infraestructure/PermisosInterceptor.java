package com.pos_backend.gateway.infraestructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Matriz de permisos por rol. ADMIN (o cuentas sin rol, por compatibilidad con
 * las que ya existían antes de este cambio) tiene acceso total siempre.
 *
 * Para agregar un rol o ajustar accesos, solo hay que tocar MATRIZ de aquí abajo
 * — el mismo criterio está espejado en el frontend en shared/utils/permisos.ts
 * para poder ocultar botones/menús sin esperar la respuesta 403 del backend,
 * pero la verdad de quién puede hacer qué vive AQUÍ, no en el frontend.
 */
@Component
public class PermisosInterceptor implements HandlerInterceptor {

    @Value("${jwt.secret}")
    private String secret;

    // Prefijo de ruta -> módulo lógico
    private static final Map<String, String> MODULO_POR_RUTA = Map.ofEntries(
            Map.entry("/api/pos/admin", "PLATAFORMA"), // panel de super admin (ver rol SUPERADMIN más abajo)
            Map.entry("/api/surtiana/inventario", "INVENTARIO"),
            Map.entry("/api/pos/categorias", "INVENTARIO"),
            Map.entry("/api/pos/proveedores", "COMPRAS"),
            Map.entry("/api/pos/compras", "COMPRAS"),
            Map.entry("/api/pos/clientes", "CLIENTES"),
            Map.entry("/api/pos/ventas", "VENTAS"),
            Map.entry("/api/pos/empresa", "CONFIGURACION"),
            Map.entry("/api/pos/facturacion", "FACTURACION"),
            Map.entry("/api/pos/contabilidad", "CONTABILIDAD"),
            Map.entry("/api/pos/nomina", "NOMINA") // todavía no existe el servicio, queda listo para cuando exista
    );

    // rol -> (modulo -> NONE | READ | FULL). Lo que no aparece = NONE.
    private static final Map<String, Map<String, String>> MATRIZ = Map.of(
            "CAJERO", Map.of(
                    "VENTAS", "FULL",
                    "CLIENTES", "FULL",
                    "INVENTARIO", "READ"
            ),
            "CONTADOR", Map.of(
                    "CONTABILIDAD", "FULL",
                    "FACTURACION", "FULL",
                    "NOMINA", "FULL",
                    "VENTAS", "READ",
                    "COMPRAS", "READ",
                    "CLIENTES", "READ",
                    "INVENTARIO", "READ"
            ),
            "INVENTARIO", Map.of(
                    "INVENTARIO", "FULL",
                    "COMPRAS", "FULL",
                    "VENTAS", "READ"
            )
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();

        // Login, registro, recuperación de clave y catálogo de planes son públicos
        if (path.startsWith("/api/pos/usuario") || path.startsWith("/api/pos/planes") || path.startsWith("/api/pos/suscripciones")) {
            return true;
        }

        String modulo = moduloDeLaRuta(path);
        if (modulo == null) return true; // ruta que no controla este filtro

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Falta el token de autenticación");
            return false;
        }

        String rol;
        try {
            String token = authHeader.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            rol = claims.get("rol", String.class);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o vencido");
            return false;
        }

        // PLATAFORMA (panel de super admin, ver AdminUseCase en facturacion-service):
        // exige SUPERADMIN exacto — ni siquiera ADMIN de una empresa normal entra acá.
        // SUPERADMIN nunca es asignable desde la app (ver ROLES_VALIDOS en
        // UsuarioUseCase.crearEmpleado), solo a mano en la base de datos.
        if ("PLATAFORMA".equals(modulo)) {
            if (!"SUPERADMIN".equalsIgnoreCase(rol)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Esto es solo para el super administrador de la plataforma");
                return false;
            }
            return true;
        }

        // ADMIN, SUPERADMIN o cuentas sin rol asignado (compatibilidad con lo que ya existía) → acceso total
        if (rol == null || rol.isBlank() || "ADMIN".equalsIgnoreCase(rol) || "SUPERADMIN".equalsIgnoreCase(rol)) return true;

        Map<String, String> permisosDelRol = MATRIZ.getOrDefault(rol.toUpperCase(), Map.of());
        String nivel = permisosDelRol.getOrDefault(modulo, "NONE");

        if ("NONE".equals(nivel)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tu rol no tiene acceso al módulo " + modulo);
            return false;
        }
        if ("READ".equals(nivel) && !"GET".equalsIgnoreCase(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tu rol solo tiene acceso de lectura en " + modulo);
            return false;
        }

        return true;
    }

    private String moduloDeLaRuta(String path) {
        for (Map.Entry<String, String> e : MODULO_POR_RUTA.entrySet()) {
            if (path.startsWith(e.getKey())) return e.getValue();
        }
        return null;
    }
}
