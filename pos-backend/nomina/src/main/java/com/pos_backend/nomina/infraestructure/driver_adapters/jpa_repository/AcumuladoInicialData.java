package com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "acumulados_iniciales_nomina",
        uniqueConstraints = @UniqueConstraint(columnNames = {"empresaId", "empleadoId", "anio"}))
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AcumuladoInicialData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long acumuladoId;

    @Column(nullable = false, length = 50) private String empresaId;
    private Long empleadoId;
    @Column(length = 150) private String nombreEmpleado;
    private Integer anio;
    private LocalDate fechaCorte;

    private Double sueldoAcumulado;
    private Double auxilioTransporteAcumulado;
    private Double extrasAcumuladas;
    private Double bonificacionesAcumuladas;
    private Double saludAcumulada;
    private Double pensionAcumulada;
    private Double retencionAcumulada;
    private Double cesantiasAcumuladas;
    private Double interesesCesantiasAcumulados;
    private Double primaAcumulada;
    private Double vacacionesAcumuladas;
    private Double diasVacacionesPendientes;

    @Column(length = 500) private String observaciones;
}
