package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.UsuarioAdminResumen;

import java.util.List;

public interface UsuarioConsultaGateway {
    List<UsuarioAdminResumen> listarTodos();
}
