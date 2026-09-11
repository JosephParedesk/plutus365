package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.NotaCredito;

public interface ContabilidadNotaGateway {
    /** Genera el asiento que reversa la venta. Lanza RuntimeException si falla. */
    void generarAsiento(NotaCredito nota, String metodoPagoOriginal, String empresaId);
}
