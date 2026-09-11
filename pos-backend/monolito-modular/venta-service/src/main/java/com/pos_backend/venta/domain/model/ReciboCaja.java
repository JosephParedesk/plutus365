package com.pos_backend.venta.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Recibo de caja: documento que soporta el dinero recibido de un cliente.
 *
 * Es el espejo del "Recibo de pago" que ya existe en Compras: allá le pagas a
 * un proveedor, aquí te paga un cliente. Un mismo recibo puede abonar a varias
 * facturas a la vez (es lo normal cuando el cliente paga un consolidado).
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ReciboCaja {
    private Long reciboId;
    private String empresaId;
    private String numeroRecibo;      // RC-00001

    private Long clienteId;
    private String clienteNombre;

    private LocalDateTime fecha;
    private LocalDate fechaRecibido;

    // ABONO_CARTERA -> se aplica a facturas concretas
    // ANTICIPO      -> el cliente paga por adelantado, sin factura todavía
    private String tipoRecibo;

    private String origenDinero;      // EFECTIVO, TRANSFERENCIA, TARJETA, CHEQUE
    private String bancoDestino;
    private String referenciaPago;    // número de transacción, cheque, etc.

    private List<AplicacionCobro> aplicaciones;
    private Double totalRecibido;

    private String estado;            // REGISTRADO, ANULADO
    private String observaciones;
    private String creadoPor;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class AplicacionCobro {
        private Long ventaId;
        private String numeroVenta;
        private Double saldoAnterior;
        private Double valorAplicado;
        private Double saldoNuevo;
    }
}
