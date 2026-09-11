package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.NotaAjusteNomina;
import java.util.List;

public interface NotaAjusteNominaGateway {
    NotaAjusteNomina guardar(NotaAjusteNomina nota);
    NotaAjusteNomina buscarPorId(Long id, String empresaId);
    NotaAjusteNomina buscarPorNominaElectronicaId(Long nominaElectronicaId, String empresaId);
    List<NotaAjusteNomina> listar(String empresaId);
    void eliminar(Long id, String empresaId);
}
