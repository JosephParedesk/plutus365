package com.pos_backend.venta.domain.model.gateway;

import com.pos_backend.venta.domain.model.NotaDebitoVenta;
import java.time.LocalDate;
import java.util.List;

public interface NotaDebitoVentaGateway {
    NotaDebitoVenta guardar(NotaDebitoVenta nota);
    NotaDebitoVenta buscarPorId(Long notaDebitoId, String empresaId);
    List<NotaDebitoVenta> listar(String empresaId);
    List<NotaDebitoVenta> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta);
    String generarSiguienteNumero(String empresaId);
}
