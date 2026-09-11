package com.pos_backend.contabilidad.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class BalanceGeneral {
    private LocalDate fechaCorte;
    private List<SaldoCuenta> activos;
    private List<SaldoCuenta> pasivos;
    private List<SaldoCuenta> patrimonio;   // incluye "Utilidad del ejercicio (acumulada, sin cerrar)"
    private Double totalActivo;
    private Double totalPasivo;
    private Double totalPatrimonio;
    private Boolean cuadra;   // totalActivo == totalPasivo + totalPatrimonio (con tolerancia de redondeo)
}
