package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.ProveedorRemota;

public interface ProveedorConsultaGateway {
    ProveedorRemota buscarProveedor(Long proveedorId, String empresaId);
}
