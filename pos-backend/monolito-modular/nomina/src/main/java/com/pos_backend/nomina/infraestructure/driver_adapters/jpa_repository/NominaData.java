package com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "nominas")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NominaData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nominaId;

    @Column(nullable = false, length = 50) private String empresaId;
    @Column(length = 30) private String numero;
    private Integer anio;
    private Integer mes;
    @Column(length = 20) private String periodicidad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaPago;
    @Column(length = 20) private String estado;

    private Double totalDevengado;
    private Double totalDeducciones;
    private Double totalNeto;
    private Double totalAportesEmpleador;
    private Double totalProvisiones;
    private Double costoTotalEmpresa;

    @Column(length = 100) private String creadoPor;
    @Column(length = 500) private String observaciones;

    @Lob @Column(columnDefinition = "LONGTEXT")
    private String detallesJson;
}
