package com.pos_backend.contabilidad.domain.model.gateway;

import com.pos_backend.contabilidad.domain.model.AsientoContable;
import java.util.List;

public interface AsientoContableGateway {
    AsientoContable guardar(AsientoContable asiento);
    AsientoContable buscarPorId(Long asientoId, String empresaId);
    AsientoContable buscarPorOrigenYReferencia(String origen, Long referenciaId, String empresaId);
    List<AsientoContable> listar(String empresaId);
    String generarSiguienteNumero(String empresaId);
}
