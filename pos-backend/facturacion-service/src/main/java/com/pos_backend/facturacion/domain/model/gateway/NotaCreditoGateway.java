package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.NotaCredito;
import java.util.List;

public interface NotaCreditoGateway {
    NotaCredito guardar(NotaCredito nota);
    NotaCredito buscarPorId(Long notaCreditoId, String empresaId);
    List<NotaCredito> listar(String empresaId);
    List<NotaCredito> listarPorFactura(Long facturaId, String empresaId);
    long contarPorEmpresa(String empresaId);
    void eliminar(Long notaCreditoId, String empresaId);
}
