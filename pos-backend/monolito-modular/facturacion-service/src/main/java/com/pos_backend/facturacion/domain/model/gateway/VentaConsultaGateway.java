package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.ClienteRemoto;
import com.pos_backend.facturacion.domain.model.EmpresaRemota;
import com.pos_backend.facturacion.domain.model.VentaRemota;

public interface VentaConsultaGateway {
    VentaRemota buscarVenta(Long ventaId, String empresaId);
}
