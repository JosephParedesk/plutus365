package com.pos_backend.venta.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Remisión: constancia de que la mercancía salió del negocio y fue entregada
 * al cliente, sin que todavía exista una factura de venta formal.
 *
 * Igual que una cotización, NO mueve inventario ni genera asiento contable por
 * sí sola — solo cuando se convierte en venta ocurren esos efectos (los aplica
 * VentaUseCase, que ya tiene toda la lógica de compensación). Si en el futuro
 * se necesita que la remisión descuente stock en el momento de la entrega
 * (antes de facturar), eso requiere tocar VentaUseCase con más cuidado, no
 * solo este archivo.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Remision {
    private Long remisionId;
    private String empresaId;
    private String numeroRemision;   // REM-00001

    private Long clienteId;
    private String clienteNombre;

    private LocalDateTime fecha;

    // BORRADOR   -> se está armando
    // ENTREGADA  -> la mercancía ya salió, esperando facturación
    // FACTURADA  -> ya se convirtió en venta (guarda ventaGeneradaId)
    // ANULADA    -> se canceló
    private String estado;

    private List<VentaItem> items;
    private Double subtotal;
    private Double descuentoTotal;
    private Double totalIva;
    private Double total;

    private Long centroCostoId;
    private String centroCostoNombre;

    private String lugarEntrega;
    private String transportador;
    private String observaciones;

    private Long ventaGeneradaId;         // se llena al facturarla
    private String creadoPor;
}
