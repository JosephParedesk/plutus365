package com.pos_backend.auth.domain.model.gateway;

import com.pos_backend.auth.domain.model.Usuario;

public interface JwtGateway {
    String generarToken(Usuario usuario);
    String extraerRol(String token);  // ← AGREGAR
    String extraerEmpresaId(String token);
    Long extraerPlanId(String token);
}