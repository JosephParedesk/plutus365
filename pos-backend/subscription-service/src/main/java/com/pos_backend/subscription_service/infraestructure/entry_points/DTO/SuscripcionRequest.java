package com.pos_backend.subscription_service.infraestructure.entry_points.DTO;


import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SuscripcionRequest {
    private String usuarioCedula;
    private Long planId;
    private String metodoPago;
}