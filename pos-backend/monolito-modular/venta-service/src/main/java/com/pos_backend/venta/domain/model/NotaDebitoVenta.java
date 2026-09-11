package com.pos_backend.venta.domain.model;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Nota débito de ventas: aumenta lo que un cliente debe sobre una venta ya
 * registrada (cargo adicional, interés de mora, ajuste). Es el espejo, del
 * lado de cartera de clientes, de la Nota Débito que ya existe en Compras
 * para proveedores.
 *
 * v1 solo ajusta el saldoPendiente de la venta referenciada — NO genera
 * asiento contable propio todavía. Eso requiere un endpoint nuevo en
 * contabilidad-service (análogo a "/desde-recibo-caja"), que queda pendiente
 * a propósito para no mezclar dos servicios en la misma pasada. Ver
 * NotaDebitoVentaUseCase.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotaDebitoVenta {
    private Long notaDebitoId;
    private String empresaId;
    private String numeroNotaDebito;   // NDV-00001

    private Long ventaReferenciaId;
    private String numeroVentaReferencia;
    private Long clienteId;
    private String clienteNombre;

    private LocalDateTime fecha;
    private String concepto;      // motivo del cargo
    private Double valor;

    private String estado;        // REGISTRADA, ANULADA
    private String observaciones;
    private String creadoPor;
}
