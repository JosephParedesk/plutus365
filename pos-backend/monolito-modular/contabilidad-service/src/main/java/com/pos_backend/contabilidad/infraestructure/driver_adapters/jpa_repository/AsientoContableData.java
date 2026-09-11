package com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "asientos_contables")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AsientoContableData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long asientoId;

    @Column(nullable = false, length = 50)
    private String empresaId;

    @Column(length = 30)
    private String numero;

    private LocalDate fecha;

    @Column(length = 255)
    private String descripcion;

    @Column(length = 20)
    private String origen;

    private Long referenciaId;

    @Column(length = 20)
    private String estado;

    private Double totalDebe;
    private Double totalHaber;

    @Column(length = 100)
    private String creadoPor;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String movimientosJson;
}
