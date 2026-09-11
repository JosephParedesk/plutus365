package com.pos_backend.venta.domain.model.gateway;

import com.pos_backend.venta.domain.model.ConfiguracionRecibo;

public interface ConfiguracionReciboGateway {
    ConfiguracionRecibo buscarPorEmpresaId(String empresaId);
    ConfiguracionRecibo guardar(ConfiguracionRecibo configuracion);
}
