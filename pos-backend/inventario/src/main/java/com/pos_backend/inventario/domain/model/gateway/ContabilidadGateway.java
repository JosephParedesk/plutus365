package com.pos_backend.inventario.domain.model.gateway;

import java.time.LocalDate;

public interface ContabilidadGateway {
    /** Registra el asiento de saldo inicial de inventario y devuelve el número del asiento generado. */
    String registrarSaldoInicialInventario(double valorTotal, String cuentaContrapartida, LocalDate fechaCorte,
                                            String empresaId, String creadoPor);
}
