package com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "centros_costo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"empresaId", "codigo"}))
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CentroCostoData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long centroCostoId;
    @Column(nullable = false, length = 50) private String empresaId;
    @Column(nullable = false, length = 30) private String codigo;
    @Column(nullable = false, length = 120) private String nombre;
    @Column(length = 255) private String descripcion;
    @Column(length = 120) private String responsable;
    private Boolean activo;
}
