package com.pos_backend.venta.domain.model.gateway;

import com.pos_backend.venta.domain.model.Cotizacion;
import java.time.LocalDate;
import java.util.List;

public interface CotizacionGateway {
    Cotizacion guardar(Cotizacion cotizacion);
    Cotizacion buscarPorId(Long cotizacionId, String empresaId);
    List<Cotizacion> listar(String empresaId);
    List<Cotizacion> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta);
    String generarSiguienteNumero(String empresaId);
}
