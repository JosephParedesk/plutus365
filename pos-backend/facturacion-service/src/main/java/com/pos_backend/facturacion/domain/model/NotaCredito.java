package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Nota crédito electrónica (DIAN).
 *
 * Reglas verificadas contra la norma vigente:
 *  - SIEMPRE referencia la factura original por su CUFE (no basta el número).
 *  - Tiene su propio identificador único: CUDE (no CUFE, que es solo de facturas).
 *  - Lleva un código de concepto de corrección (ver ConceptoNotaCredito).
 *  - DIAN Concepto 1607: el mecanismo legal para variar el valor de una venta ya
 *    facturada NO es anular y reexpedir la factura, sino emitir esta nota.
 *  - El número de una factura anulada NO se reutiliza.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotaCredito {
    private Long notaCreditoId;
    private String empresaId;

    private Long facturaId;            // factura que corrige
    private String numeroFactura;      // copia, para mostrar sin joins
    private String cufeFactura;        // CUFE de la factura original (obligatorio ante la DIAN)

    private String referenceCode;      // el que se le mandó a Factus — hace falta para poder eliminarla si quedó pendiente
    private String numeroNota;         // asignado por Factus, ej: NC634
    private String cude;               // identificador único de la nota
    private String qrUrl;
    private String urlDocumento;       // vista pública del documento en Factus
    private String ambiente;           // PRUEBAS | PRODUCCION

    private String conceptoCodigo;     // 1..6 según el anexo técnico
    private String conceptoDescripcion;
    private String motivo;             // texto libre que escribe el usuario

    private Boolean anulaTotal;        // true = anula la factura completa (concepto 02)
    private List<ItemNotaCredito> items;

    private Double subtotal;
    private Double totalIva;
    private Double total;

    private String estado;             // GENERADA, ACEPTADA, RECHAZADA, ERROR
    private String respuestaDian;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    private String creadoPor;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class ItemNotaCredito {
        private String sku;
        private String descripcion;
        private Double cantidad;
        private Double precioUnitario;
        private Double porcentajeIva;
        private Double valorIva;
        private Double valorTotal;
    }
}
