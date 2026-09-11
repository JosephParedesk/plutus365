package com.pos_backend.subscription_service.domain.model;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Suscripcion {
    private Long id;
    private String usuarioCedula;
    private Long planId;
    private String estado;        // PENDIENTE, ACTIVA, VENCIDA, CANCELADA
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaCreacion;
    private String metodoPago;
}