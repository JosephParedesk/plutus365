package com.pos_backend.compra.domain.model.gateway;

import com.pos_backend.compra.domain.model.Compra;

public interface ContabilidadGateway {
    /** Genera el asiento contable de una compra ya registrada. Lanza RuntimeException si falla. */
    void generarAsientoCompra(Compra compra, String empresaId);
}
