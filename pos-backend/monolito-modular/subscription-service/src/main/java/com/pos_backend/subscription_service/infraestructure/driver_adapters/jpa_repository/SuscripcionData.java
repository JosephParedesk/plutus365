package com.pos_backend.subscription_service.infraestructure.driver_adapters.jpa_repository;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "suscripciones")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SuscripcionData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuarioCedula;
    private Long planId;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaCreacion;
    private String metodoPago;
}