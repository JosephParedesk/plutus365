package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.NotaDebito;
import java.util.List;

public interface NotaDebitoGateway {
    NotaDebito guardar(NotaDebito nota);
    NotaDebito buscarPorId(Long notaDebitoId, String empresaId);
    List<NotaDebito> listar(String empresaId);
    List<NotaDebito> listarPorFactura(Long facturaId, String empresaId);
    void eliminar(Long notaDebitoId, String empresaId);
}
