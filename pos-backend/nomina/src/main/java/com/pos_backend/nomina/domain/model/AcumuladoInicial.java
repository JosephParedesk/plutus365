package com.pos_backend.nomina.domain.model;

import lombok.*;
import java.time.LocalDate;

/**
 * Saldos que un empleado YA traía causados antes de empezar a usar Plutus365.
 *
 * Para qué sirve: si llevas la nómina en otro sistema (o en Excel) y migras a
 * mitad de año, tus reportes acumulados solo contarían desde la primera nómina
 * liquidada aquí — y quedarían cortos. Estos valores se suman a lo liquidado en
 * el sistema para que los acumulados reflejen el año completo.
 *
 * Se captura UNA sola vez por empleado y año.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AcumuladoInicial {
    private Long acumuladoId;
    private String empresaId;

    private Long empleadoId;
    private String nombreEmpleado;
    private Integer anio;
    /** Fecha de corte de los saldos traídos (último día liquidado en el sistema anterior). */
    private LocalDate fechaCorte;

    // Devengados acumulados
    private Double sueldoAcumulado;
    private Double auxilioTransporteAcumulado;
    private Double extrasAcumuladas;
    private Double bonificacionesAcumuladas;

    // Aportes y deducciones acumuladas
    private Double saludAcumulada;
    private Double pensionAcumulada;
    private Double retencionAcumulada;

    // Prestaciones ya causadas y NO pagadas (lo más importante de migrar)
    private Double cesantiasAcumuladas;
    private Double interesesCesantiasAcumulados;
    private Double primaAcumulada;
    private Double vacacionesAcumuladas;
    /** Días de vacaciones pendientes de disfrutar. */
    private Double diasVacacionesPendientes;

    private String observaciones;
}
