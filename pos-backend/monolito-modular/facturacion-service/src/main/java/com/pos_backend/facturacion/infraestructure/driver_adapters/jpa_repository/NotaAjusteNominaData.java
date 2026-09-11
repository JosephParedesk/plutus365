package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notas_ajuste_nomina")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotaAjusteNominaData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notaAjusteNominaId;

    @Column(nullable = false, length = 50) private String empresaId;
    private Long nominaElectronicaId;
    @Column(length = 40) private String numeroNominaElectronica;

    @Column(length = 100) private String referenceCode;
    @Column(length = 40) private String numeroAjuste;
    @Column(length = 20) private String ambiente;

    @Column(length = 20) private String estado;
    @Column(length = 1000) private String respuestaDian;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    @Column(length = 100) private String creadoPor;
}
