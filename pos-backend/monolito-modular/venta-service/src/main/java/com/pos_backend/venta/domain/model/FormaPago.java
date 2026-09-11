package com.pos_backend.venta.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FormaPago {
    private Long formaPagoId;
    private String metodo;   // EFECTIVO, TARJETA, TRANSFERENCIA, MIXTO
    private Double valor;
}
