package com.pos_backend.nomina.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Nomina {
    private Long nominaId;
    private String empresaId;
    private String numero;              // NOM-00001

    private Integer anio;
    private Integer mes;
    private String periodicidad;        // MENSUAL, QUINCENAL
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaPago;

    private String estado;              // BORRADOR, LIQUIDADA, PAGADA, ANULADA

    private List<NominaDetalle> detalles;

    // Totales del período (suma de todos los empleados)
    private Double totalDevengado;
    private Double totalDeducciones;
    private Double totalNeto;
    private Double totalAportesEmpleador;
    private Double totalProvisiones;
    private Double costoTotalEmpresa;   // neto + aportes empleador + provisiones

    private String creadoPor;
    private String observaciones;
}
