package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "documentos_soporte")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class DocumentoSoporteData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentoSoporteId;

    @Column(nullable = false, length = 50) private String empresaId;
    private Long compraId;
    @Column(length = 60) private String numeroComprobante;
    private Long proveedorId;
    @Column(length = 200) private String proveedorNombre;
    @Column(length = 100) private String referenceCode;
    @Column(length = 40) private String numeroDocumento;
    @Column(length = 200) private String cude;
    @Column(length = 500) private String qrUrl;
    @Column(length = 500) private String urlDocumento;
    @Column(length = 20) private String ambiente;
    private Double subtotal;
    private Double totalIva;
    private Double totalRetencion;
    private Double total;
    @Column(length = 20) private String estado;
    @Column(length = 1000) private String respuestaDian;

    @Lob @Column(columnDefinition = "TEXT") private String itemsJson;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    @Column(length = 100) private String creadoPor;
}
