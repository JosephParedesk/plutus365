package com.pos_backend.compra.domain.model;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FormaPago {
    private Long formaPagoId;
    private String metodo;   // EFECTIVO, TRANSFERENCIA, TARJETA, CREDITO_PROVEEDOR
    private Double valor;
    private LocalDate fechaVencimiento;   // solo aplica si metodo = CREDITO_PROVEEDOR
}