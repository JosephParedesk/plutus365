package com.pos_backend.contabilidad.domain.model.gateway;

import com.pos_backend.contabilidad.domain.model.CuentaContable;
import java.util.List;

public interface PucBaseGateway {
    /** Devuelve el set base del PUC, sin empresaId asignado todavía. */
    List<CuentaContable> cargarBase();
}
