package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.NominaRemota;

public interface NominaConsultaGateway {
    NominaRemota buscarNomina(Long nominaId, String empresaId);
}
