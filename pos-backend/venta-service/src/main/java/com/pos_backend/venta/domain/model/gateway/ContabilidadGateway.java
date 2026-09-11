package com.pos_backend.venta.domain.model.gateway;

public interface ContabilidadGateway {
    /** Genera el asiento contable de una venta ya registrada. Lanza RuntimeException si falla. */
    void generarAsientoVenta(com.pos_backend.venta.domain.model.Venta venta, String empresaId);
}
