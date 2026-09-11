package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Nota débito electrónica (DIAN) — cargo adicional sobre una factura ya validada
 * (intereses, gastos por cobrar, cambio de valor). Es el mecanismo legal aparte de
 * la nota crédito: mientras esa resta valor a una factura, esta lo suma.
 *
 * La doc de Factus dice que el cliente es opcional (lo toma de la factura referenciada
 * por `bill_number` si no se envía — developers.factus.com.co/notas-debito/descripcion-de-campos/,
 * verificado 2026-09-05) pero contra el sandbox compartido esa resolución automática
 * no funcionó (rechazó pidiendo customer completo) — se manda igual, siempre, mismo
 * patrón que NotaCredito.
 *
 * No confundir con NotaDebitoVenta (venta-service): esa es un ajuste interno de
 * cartera (aumenta el saldoPendiente) que no se transmite a la DIAN. Esta sí.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotaDebito {
    private Long notaDebitoId;
    private String empresaId;

    private Long facturaId;            // factura que se carga
    private String numeroFactura;      // copia, para mostrar sin joins
    private String cufeFactura;        // CUFE de la factura original, solo para mostrar

    private String referenceCode;      // el que se le mandó a Factus — hace falta para poder eliminarla si quedó pendiente
    private String numeroNota;         // asignado por Factus, ej: ND634
    private String cude;               // identificador único de la nota
    private String qrUrl;
    private String urlDocumento;       // vista pública del documento en Factus
    private String ambiente;           // PRUEBAS | PRODUCCION

    private String conceptoCodigo;     // 1..4 según la tabla de Factus
    private String conceptoDescripcion;
    private String motivo;             // texto libre que escribe el usuario

    private List<ItemNotaDebito> items;

    private Double subtotal;
    private Double totalIva;
    private Double total;

    private String estado;             // GENERADA, ACEPTADA, RECHAZADA, ERROR
    private String respuestaDian;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    private String creadoPor;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class ItemNotaDebito {
        private String sku;
        private String descripcion;
        private Double cantidad;
        private Double precioUnitario;
        private Double porcentajeIva;
        private Double valorIva;
        private Double valorTotal;
    }
}
