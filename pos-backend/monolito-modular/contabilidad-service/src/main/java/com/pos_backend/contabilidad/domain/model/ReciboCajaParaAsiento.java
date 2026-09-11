package com.pos_backend.contabilidad.domain.model;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReciboCajaParaAsiento {
    private Long reciboId;
    private String numeroRecibo;
    private LocalDate fecha;
    private Double total;
    private String origenDinero;   // EFECTIVO -> caja; el resto -> bancos
    private Boolean esAnticipo;
}
