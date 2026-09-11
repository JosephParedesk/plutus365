package com.pos_backend.nomina.domain.model;

import lombok.*;
import java.util.List;

/** Liquidación de un empleado dentro de un período de nómina. */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NominaDetalle {
    private Long empleadoId;
    private String nombreEmpleado;
    private String numeroDocumento;
    private String cargo;

    private Double salarioBase;
    private Integer diasTrabajados;

    // ── Devengados ──
    private Double sueldo;
    private Double auxilioTransporte;
    // Horas extra/recargos: uno por tipo (los 7 códigos DIAN de Factus — ver
    // developers.factus.com.co/tablas-de-referencia/tablas-referencia-nomina/).
    // Cantidad y porcentaje se capturan explícitos (no se derivan) porque el
    // divisor de "valor hora ordinaria" cambió con la reducción gradual de la
    // jornada (Ley 2101/2021, 48h→42h) y el recargo dominical/festivo sigue en
    // implementación gradual (Ley 2466/2025: 80% jul/25, 90% jul/26, 100% jul/27)
    // — más seguro que el contador confirme el valor a que nosotros lo calculemos mal.
    private List<HoraExtraItem> horasExtra;
    private Double comisiones;
    private Double bonificaciones;
    private Double otrosDevengados;
    private Double totalDevengado;

    /** Base sobre la que se calculan aportes (excluye auxilio de transporte). */
    private Double ibc;

    // ── Deducciones al empleado ──
    private Double saludEmpleado;
    private Double pensionEmpleado;
    private Double fondoSolidaridad;
    private Double retencionFuente;
    private Double prestamos;
    private Double otrasDeducciones;
    private Double totalDeducciones;

    private Double netoPagar;

    // ── Aportes del empleador (costo, no se descuenta al empleado) ──
    private Double saludEmpleador;
    private Double pensionEmpleador;
    private Double arl;
    private Double sena;
    private Double icbf;
    private Double cajaCompensacion;
    private Double totalAportesEmpleador;
    private Boolean exonerado;   // Ley 1607/2012: sin salud 8.5%, SENA ni ICBF

    // ── Provisiones de prestaciones sociales ──
    private Double provCesantias;
    private Double provInteresesCesantias;
    private Double provPrima;
    private Double provVacaciones;
    private Double totalProvisiones;

    private Double costoTotal;

    /** tipoCode: 1..7, ver ConceptoHoraExtra. */
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class HoraExtraItem {
        private Integer tipoCode;
        private Double cantidadHoras;
        private Double porcentaje;
        private Double valor;
    }
}
