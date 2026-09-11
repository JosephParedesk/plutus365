package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Nota de ajuste a documento soporte (DIAN), transmitida vía Factus
 * (`/v2/adjustment-notes/validate`) — corrige o anula un documento soporte ya
 * ACEPTADO. Los ítems y el proveedor se copian del documento soporte original
 * (reutiliza `DocumentoSoporte.ItemDocumentoSoporte`, no tiene sentido duplicar
 * la clase): esta nota no permite negociar cantidades distintas a las ya
 * transmitidas, solo corregirlas o anularlas.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotaAjusteDocumentoSoporte {
    private Long notaAjusteId;
    private String empresaId;

    private Long documentoSoporteId;           // documento soporte que se corrige
    private String numeroDocumentoSoporte;     // copia, para mostrar sin joins ("support_document_number")
    private String cudeDocumentoSoporte;

    private String referenceCode;
    private String numeroNota;                 // asignado por Factus, ej: NA1
    private String cude;
    private String qrUrl;
    private String ambiente;

    private String conceptoCodigo;             // ver ConceptoNotaAjuste
    private String conceptoDescripcion;
    private String observacion;

    private List<DocumentoSoporte.ItemDocumentoSoporte> items;

    private Double subtotal;
    private Double totalIva;
    private Double totalRetencion;
    private Double total;

    private String estado;                     // GENERADA, RECHAZADA, ERROR
    private String respuestaDian;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    private String creadoPor;
}
