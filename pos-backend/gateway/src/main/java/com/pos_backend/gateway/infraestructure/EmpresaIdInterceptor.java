package com.pos_backend.gateway.infraestructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Extrae empresaId del JWT y lo inyecta como header X-Empresa-Id.
 *
 * Tiene que ser un Filter, no un HandlerInterceptor: los interceptores corren
 * dentro del DispatcherServlet, después de que Spring Cloud Gateway MVC ya
 * capturó el HttpServletRequest que va a reenviar al microservicio downstream,
 * así que no pueden agregarle un header a esa copia. Un Filter sí puede,
 * envolviendo el request con chain.doFilter(wrapper, response) antes de que
 * el gateway lo procese.
 */
@Component
public class EmpresaIdInterceptor implements Filter {

    private static final String HEADER = "X-Empresa-Id";

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = httpRequest.getHeader("Authorization");
        String empresaId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
                empresaId = claims.get("empresaId", String.class);
            } catch (Exception e) {
                // token inválido, deja pasar sin empresaId
            }
        }

        chain.doFilter(empresaId != null ? new EmpresaIdHeaderWrapper(httpRequest, empresaId) : request, response);
    }

    private static class EmpresaIdHeaderWrapper extends HttpServletRequestWrapper {
        private final String empresaId;

        EmpresaIdHeaderWrapper(HttpServletRequest request, String empresaId) {
            super(request);
            this.empresaId = empresaId;
        }

        @Override
        public String getHeader(String name) {
            return HEADER.equalsIgnoreCase(name) ? empresaId : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return HEADER.equalsIgnoreCase(name)
                    ? Collections.enumeration(List.of(empresaId))
                    : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            if (!names.contains(HEADER)) names.add(HEADER);
            return Collections.enumeration(names);
        }
    }
}
