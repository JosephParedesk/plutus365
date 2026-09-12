package com.pos_backend.empresa.domain.model.gateway;

import com.pos_backend.empresa.domain.model.Empresa;

import java.util.List;

public interface EmpresaGateway {
    Empresa guardarEmpresa(Empresa empresa);
    Empresa buscarPorEmpresaId(String empresaId);
    boolean existePorEmpresaId(String empresaId);
    List<Empresa> listarTodas();
}
