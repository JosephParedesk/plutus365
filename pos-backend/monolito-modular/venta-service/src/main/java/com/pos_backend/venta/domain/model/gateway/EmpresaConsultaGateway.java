package com.pos_backend.venta.domain.model.gateway;

import com.pos_backend.venta.domain.model.EmpresaRemota;

public interface EmpresaConsultaGateway {
    EmpresaRemota buscarEmpresa(String empresaId);
}
