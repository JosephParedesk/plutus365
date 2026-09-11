package com.pos_backend.auth.infraestructure.security;

import com.pos_backend.auth.domain.model.Usuario;
import com.pos_backend.auth.domain.model.gateway.JwtGateway;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtGatewayImpl implements JwtGateway {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Override
    public String generarToken(Usuario usuario) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("cedula", usuario.getCedula())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol())
                .claim("planId", usuario.getPlanId())
                .claim("empresaId", usuario.getEmpresaId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    @Override
    public String extraerRol(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("rol", String.class);
    }

    @Override
    public String extraerEmpresaId(String token) {
        return getClaims(token).get("empresaId", String.class);
    }

    @Override
    public Long extraerPlanId(String token) {
        return getClaims(token).get("planId", Long.class);
    }

    private Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
