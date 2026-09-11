package com.pos_backend.contabilidad.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SaldoCuenta {
    private String codigo;
    private String nombre;
    private String nivel;
    private Double saldo;   // ya en el sentido natural de la cuenta (positivo = saldo normal)
}
