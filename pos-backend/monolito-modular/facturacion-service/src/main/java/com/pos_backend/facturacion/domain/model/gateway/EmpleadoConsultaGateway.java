package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.EmpleadoRemota;

public interface EmpleadoConsultaGateway {
    EmpleadoRemota buscarEmpleado(Long empleadoId, String empresaId);
}
