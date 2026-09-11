package com.pos_backend.contabilidad.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CambiosPatrimonio {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<LineaPatrimonio> lineas;
    private Double totalSaldoInicial;
    private Double totalVariacion;
    private Double totalSaldoFinal;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class LineaPatrimonio {
        private String codigo;
        private String concepto;
        private Double saldoInicial;
        private Double variacion;
        private Double saldoFinal;
    }
}
