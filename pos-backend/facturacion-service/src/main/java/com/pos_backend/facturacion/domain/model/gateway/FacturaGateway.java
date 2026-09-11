package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.Factura;
import java.util.List;

public interface FacturaGateway {
    Factura guardar(Factura factura);
    Factura buscarPorId(Long facturaId, String empresaId);
    Factura buscarPorVentaId(Long ventaId, String empresaId);
    List<Factura> listar(String empresaId);
    void eliminar(Long facturaId, String empresaId);
}
