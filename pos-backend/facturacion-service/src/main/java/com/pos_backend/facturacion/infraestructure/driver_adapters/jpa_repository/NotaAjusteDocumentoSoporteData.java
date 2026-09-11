package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notas_ajuste_documento_soporte")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotaAjusteDocumentoSoporteData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notaAjusteId;

    @Column(nullable = false, length = 50) private String empresaId;
    private Long documentoSoporteId;
    @Column(length = 60) private String numeroDocumentoSoporte;
    @Column(length = 200) private String cudeDocumentoSoporte;

    @Column(length = 100) private String referenceCode;
    @Column(length = 40) private String numeroNota;
    @Column(length = 200) private String cude;
    @Column(length = 500) private String qrUrl;
    @Column(length = 20) private String ambiente;

    @Column(length = 5) private String conceptoCodigo;
    @Column(length = 200) private String conceptoDescripcion;
    @Column(length = 250) private String observacion;

    @Lob @Column(columnDefinition = "TEXT") private String itemsJson;

    private Double subtotal;
    private Double totalIva;
    private Double totalRetencion;
    private Double total;

    @Column(length = 20) private String estado;
    @Column(length = 1000) private String respuestaDian;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    @Column(length = 100) private String creadoPor;
}
