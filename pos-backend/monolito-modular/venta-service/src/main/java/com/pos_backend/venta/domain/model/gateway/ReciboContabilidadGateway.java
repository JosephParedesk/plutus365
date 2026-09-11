package com.pos_backend.venta.domain.model.gateway;

import com.pos_backend.venta.domain.model.ReciboCaja;

public interface ReciboContabilidadGateway {
    /** Debe: Caja/Bancos · Haber: Clientes. Lanza RuntimeException si falla. */
    void generarAsientoRecibo(ReciboCaja recibo, String empresaId);
}
