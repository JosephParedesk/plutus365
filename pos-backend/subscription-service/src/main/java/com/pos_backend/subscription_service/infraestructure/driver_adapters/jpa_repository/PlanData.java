package com.pos_backend.subscription_service.infraestructure.driver_adapters.jpa_repository;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "planes")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class PlanData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer maxPerfiles;
    private Boolean activo;
}