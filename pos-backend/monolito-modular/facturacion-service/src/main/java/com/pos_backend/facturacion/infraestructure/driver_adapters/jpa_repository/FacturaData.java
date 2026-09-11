package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas", uniqueConstraints = @UniqueConstraint(
        name = "uq_facturas_empresa_numero", columnNames = {"empresa_id", "numero_factura"}))
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FacturaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facturaId;

    @Column(nullable = false, length = 50)
    private String empresaId;

    @Column(nullable = false)
    private Long ventaId;

    @Column(length = 100)
    private String referenceCode;

    @Column(length = 30)
    private String numeroFactura;

    @Column(length = 120)
    private String cufe;

    @Column(length = 255)
    private String qrUrl;

    @Column(length = 255)
    private String urlDocumento;

    @Column(length = 20)
    private String ambiente;

    @Column(length = 20)
    private String estado;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String respuestaDian;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
}
