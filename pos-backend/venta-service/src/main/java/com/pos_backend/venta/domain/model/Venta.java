package com.pos_backend.venta.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Venta {
    private Long ventaId;
    private String empresaId;
    private String numeroVenta;       // VT-00001

    private Long clienteId;           // null = consumidor final
    private String clienteNombre;

    private LocalDateTime fecha;
    private String estado;            // REGISTRADA, ANULADA
    // ── Cartera: venta a crédito ──
    private Boolean tieneCreditoCliente;
    private java.time.LocalDate fechaVencimientoCredito;
    private Double saldoPendiente;   // lo que el cliente aún debe de esta venta

    private Long centroCostoId;
    private String centroCostoNombre;
    private Boolean esObsequio;   // venta sin cobro (muestra, cortesía) — no genera ingreso
    private String creadoPor;

    private List<VentaItem> items;
    private List<FormaPago> formasPago;

    private Double subtotal;
    private Double descuentoTotal;
    private Double totalIva;
    private Double total;
}
