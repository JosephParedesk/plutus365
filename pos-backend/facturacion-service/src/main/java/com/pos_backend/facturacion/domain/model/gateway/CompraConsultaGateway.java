package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.CompraRemota;

public interface CompraConsultaGateway {
    CompraRemota buscarCompra(Long compraId, String empresaId);
}
