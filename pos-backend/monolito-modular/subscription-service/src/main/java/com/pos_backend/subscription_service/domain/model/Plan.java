package com.pos_backend.subscription_service.domain.model;



import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Plan {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer maxPerfiles;
    private Boolean activo;

}