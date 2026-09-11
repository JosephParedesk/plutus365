package com.pos_backend.contabilidad.domain.model.gateway;

import com.pos_backend.contabilidad.domain.model.CentroCosto;
import java.util.List;

public interface CentroCostoGateway {
    CentroCosto guardar(CentroCosto centroCosto);
    CentroCosto buscarPorId(Long centroCostoId, String empresaId);
    boolean existeCodigo(String codigo, String empresaId);
    List<CentroCosto> listar(String empresaId);
    void eliminar(Long centroCostoId, String empresaId);
}
