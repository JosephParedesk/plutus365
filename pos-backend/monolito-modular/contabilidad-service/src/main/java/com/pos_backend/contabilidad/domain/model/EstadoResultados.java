package com.pos_backend.contabilidad.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class EstadoResultados {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<SaldoCuenta> ingresos;
    private List<SaldoCuenta> costos;
    private List<SaldoCuenta> gastos;
    private Double totalIngresos;
    private Double totalCostos;
    private Double totalGastos;
    private Double utilidad;   // ingresos - costos - gastos
}
