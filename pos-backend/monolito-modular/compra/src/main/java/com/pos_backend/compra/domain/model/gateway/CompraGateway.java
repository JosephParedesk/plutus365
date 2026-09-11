package com.pos_backend.compra.domain.model.gateway;

import com.pos_backend.compra.domain.model.Compra;
import java.time.LocalDate;
import java.util.List;

public interface CompraGateway {
    Compra guardarCompra(Compra compra);
    Compra buscarCompraPorId(Long compraId, String empresaId);
    List<Compra> listarCompras(String empresaId);
    List<Compra> buscarConFiltros(
            String empresaId,
            Long proveedorId,
            String tipoTransaccion,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String creadoPor
    );
    void anularCompra(Long compraId, String empresaId);
    String generarSiguienteNumero(String empresaId, String tipoTransaccion);

    // Cuentas por pagar (crédito a proveedores) que vencen entre hoy y hoy+dias
    List<Compra> proximasAVencer(String empresaId, int dias);
}