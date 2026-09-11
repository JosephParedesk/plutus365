package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.EmpresaRemota;

public interface EmpresaConsultaGateway {
    EmpresaRemota buscarEmpresa(String empresaId);
}
