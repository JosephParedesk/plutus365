package com.pos_backend.inventario.domain.model.gateway;

import com.pos_backend.inventario.domain.model.MovimientoInventario;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioGateway {
    MovimientoInventario guardar(MovimientoInventario movimiento);
    List<MovimientoInventario> listarPorSku(String sku, String empresaId);
    List<MovimientoInventario> listarPorRango(String empresaId, LocalDateTime desde, LocalDateTime hasta);
}
