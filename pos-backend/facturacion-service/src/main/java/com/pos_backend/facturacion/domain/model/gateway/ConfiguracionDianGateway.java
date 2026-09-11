package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;

public interface ConfiguracionDianGateway {
    ConfiguracionDian guardar(ConfiguracionDian configuracion);
    ConfiguracionDian buscarPorEmpresaId(String empresaId);
}
