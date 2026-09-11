package com.pos_backend.contabilidad.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Estado de flujos de efectivo por el método indirecto: parte de la utilidad
 * del período y le suma/resta las variaciones de las cuentas del balance para
 * llegar a cuánto efectivo se movió realmente.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FlujoEfectivo {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private Double utilidadPeriodo;

    private List<LineaFlujo> operacion;
    private List<LineaFlujo> inversion;
    private List<LineaFlujo> financiacion;

    private Double flujoOperacion;
    private Double flujoInversion;
    private Double flujoFinanciacion;
    private Double variacionNeta;

    private Double efectivoInicial;
    private Double efectivoFinal;
    /** efectivoInicial + variacionNeta debe dar efectivoFinal (tolerancia de redondeo). */
    private Boolean cuadra;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class LineaFlujo {
        private String codigo;
        private String concepto;
        private Double valor;   // positivo = entra efectivo, negativo = sale
    }
}
