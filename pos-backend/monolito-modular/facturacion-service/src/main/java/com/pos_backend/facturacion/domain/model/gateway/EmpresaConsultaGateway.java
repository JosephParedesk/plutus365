package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.EmpresaRemota;

import java.util.List;

public interface EmpresaConsultaGateway {
    EmpresaRemota buscarEmpresa(String empresaId);
    List<EmpresaRemota> listarTodas();
}
