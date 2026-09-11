package com.pos_backend.contabilidad.domain.model;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SaldoInicialInventarioParaAsiento {
    private LocalDate fechaCorte;
    private Double valorTotal;
    private String cuentaContrapartida;  // elegida por el usuario/contador, no la adivina el sistema
}
