package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_dian")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ConfiguracionDianData {

    @Id
    @Column(length = 50)
    private String empresaId;

    @Column(length = 100)
    private String factusClientId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String factusClientSecret;

    @Column(length = 150)
    private String factusUsername;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String factusPassword;

    private Long facturaNumberingRangeId;
    private Long notaCreditoNumberingRangeId;
    private Long notaDebitoNumberingRangeId;
    private Long documentoSoporteNumberingRangeId;
    @Column(length = 50) private String nominaNumberingRangeId;
    private Long notaAjusteDocumentoSoporteNumberingRangeId;
    @Column(length = 50) private String notaAjusteNominaNumberingRangeId;

    private Boolean activo;
}
