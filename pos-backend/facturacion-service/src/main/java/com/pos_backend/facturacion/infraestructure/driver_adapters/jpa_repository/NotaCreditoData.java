package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notas_credito")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotaCreditoData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notaCreditoId;

    @Column(nullable = false, length = 50) private String empresaId;
    private Long facturaId;
    @Column(length = 60) private String numeroFactura;
    @Column(length = 200) private String cufeFactura;
    @Column(length = 100) private String referenceCode;
    @Column(length = 40) private String numeroNota;
    @Column(length = 200) private String cude;
    @Column(length = 500) private String qrUrl;
    @Column(length = 500) private String urlDocumento;
    @Column(length = 20) private String ambiente;
    @Column(length = 5) private String conceptoCodigo;
    @Column(length = 150) private String conceptoDescripcion;
    @Column(length = 500) private String motivo;
    private Boolean anulaTotal;
    private Double subtotal;
    private Double totalIva;
    private Double total;
    @Column(length = 20) private String estado;
    @Column(length = 1000) private String respuestaDian;

    @Lob @Column(columnDefinition = "TEXT") private String itemsJson;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    @Column(length = 100) private String creadoPor;
}
