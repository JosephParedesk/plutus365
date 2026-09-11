package com.pos_backend.venta.domain.model.gateway;

import com.pos_backend.venta.domain.model.Venta;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface VentaGateway {
    Venta guardarVenta(Venta venta);
    Venta buscarVentaPorId(Long ventaId, String empresaId);
    List<Venta> listarVentas(String empresaId);
    List<Venta> buscarConFiltros(
            String empresaId,
            Long clienteId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
    // Rango exacto de fecha/hora — lo usa el cierre de caja para no contar ventas fuera del turno
    List<Venta> listarPorRangoExacto(String empresaId, LocalDateTime desde, LocalDateTime hasta);
    void anularVenta(Long ventaId, String empresaId);
    String generarSiguienteNumero(String empresaId);
}
