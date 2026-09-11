package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NominaRemota {
    private Long nominaId;
    private Integer anio;
    private Integer mes;
    private String periodicidad;   // MENSUAL, QUINCENAL
    private LocalDate fechaPago;
    private String estado;         // BORRADOR, LIQUIDADA, PAGADA, ANULADA
    private List<DetalleRemoto> detalles;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class DetalleRemoto {
        private Long empleadoId;
        private Integer diasTrabajados;
        private Double sueldo;
        private Double auxilioTransporte;
        private List<HoraExtraRemota> horasExtra;
        private Double comisiones;
        private Double bonificaciones;
        private Double otrosDevengados;
        private Double ibc;
        private Double saludEmpleado;
        private Double pensionEmpleado;
        private Double fondoSolidaridad;
        private Double retencionFuente;
        private Double prestamos;
        private Double otrasDeducciones;
        private Double netoPagar;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class HoraExtraRemota {
        private Integer tipoCode;
        private Double cantidadHoras;
        private Double porcentaje;
        private Double valor;
    }
}
