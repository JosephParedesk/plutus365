package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Documento soporte electrónico (DIAN) — obligatorio desde la Resolución 000488/2022
 * (complementa la 000167/2021) cuando la empresa compra a un proveedor que NO está
 * obligado a facturar electrónicamente. A diferencia de una factura, acá la empresa
 * es la que COMPRA, no la que vende: se transmite el `provider` (el proveedor), no
 * un cliente. Referencia developers.factus.com.co/documentos-soporte/.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class DocumentoSoporte {
    private Long documentoSoporteId;
    private String empresaId;

    private Long compraId;             // compra que respalda
    private String numeroComprobante;  // copia, para mostrar sin joins
    private Long proveedorId;
    private String proveedorNombre;

    private String referenceCode;      // el que se le mandó a Factus — hace falta para poder eliminarlo si quedó pendiente
    private String numeroDocumento;    // asignado por Factus, ej: DS634
    private String cude;               // identificador único
    private String qrUrl;
    private String urlDocumento;
    private String ambiente;           // PRUEBAS | PRODUCCION

    private List<ItemDocumentoSoporte> items;

    private Double subtotal;
    private Double totalIva;
    private Double totalRetencion;
    private Double total;

    private String estado;             // ACEPTADA, RECHAZADA, ERROR
    private String respuestaDian;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    private String creadoPor;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class ItemDocumentoSoporte {
        private String sku;
        private String descripcion;
        private Double cantidad;
        private Double precioUnitario;     // base gravable, sin IVA
        private Double descuento;
        private Double porcentajeIva;
        private Double valorIva;
        private Double porcentajeRetencion;
        private Double valorRetencion;
        private Double valorTotal;
    }
}
