package com.pos_backend.venta.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cotización / propuesta comercial.
 *
 * A diferencia de una venta, una cotización NO mueve inventario ni genera
 * asiento contable: es una oferta, no una operación. Solo cuando se convierte
 * en venta ocurren esos efectos.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Cotizacion {
    private Long cotizacionId;
    private String empresaId;
    private String numeroCotizacion;   // COT-00001

    private Long clienteId;
    private String clienteNombre;
    private String clienteCorreo;

    private LocalDateTime fecha;
    private LocalDate fechaVencimiento;

    // BORRADOR  -> se está armando, todavía no se envía
    // ENVIADA   -> se le mandó al cliente, esperando respuesta
    // APROBADA  -> el cliente la aceptó (lista para convertir en venta)
    // RECHAZADA -> el cliente la rechazó
    // VENCIDA   -> pasó la fecha de vencimiento sin respuesta
    // CONVERTIDA-> ya se convirtió en venta (guarda ventaGeneradaId)
    private String estado;

    private List<VentaItem> items;
    private Double subtotal;
    private Double descuentoTotal;
    private Double totalIva;
    private Double total;

    private Long centroCostoId;
    private String centroCostoNombre;

    private String lugarEmision;

    // Contacto del cliente para esta cotización puntual (puede cambiar entre cotizaciones
    // del mismo cliente, por eso vive aquí y no en la ficha del cliente).
    private String contactoNombre;
    private String contactoCargo;

    // ── Condiciones comerciales, separadas para que se impriman ordenadas ──
    private String formaPago;
    private String tiempoEntrega;
    private String lugarEntrega;
    private String transporte;          // quién lo asume y si está incluido
    private String tiempoFabricacion;   // solo si aplica
    private String instalacion;         // solo si aplica
    private String capacitacion;        // solo si aplica

    // ── Garantía ──
    private String garantiaTiempo;
    private String garantiaCubre;
    private String garantiaNoCubre;
    private String garantiaComoHacerEfectiva;

    private String observaciones;

    // ── Asesor comercial que la elabora ──
    private String asesorNombre;
    private String asesorCargo;
    private String asesorTelefono;
    private String asesorCorreo;

    private Long ventaGeneradaId;            // se llena al convertirla
    private String creadoPor;                // el vendedor que la elaboró
}
