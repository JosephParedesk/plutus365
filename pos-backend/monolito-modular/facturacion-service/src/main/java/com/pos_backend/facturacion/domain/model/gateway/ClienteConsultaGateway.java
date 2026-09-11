package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.ClienteRemoto;

public interface ClienteConsultaGateway {
    ClienteRemoto buscarCliente(Long clienteId, String empresaId);
}
