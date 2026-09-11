package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Nómina electrónica (DIAN) de UN empleado en UN período — Factus recibe un
 * trabajador por solicitud (`/v2/payrolls`), así que una `Nomina` liquidada con
 * varios empleados genera varias `NominaElectronica`, una por cada uno.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NominaElectronica {
    private Long nominaElectronicaId;
    private String empresaId;

    private Long nominaId;
    private Long empleadoId;
    private String nombreEmpleado;     // copia, para mostrar sin joins
    private Integer anio;
    private Integer mes;

    private String referenceCode;      // el que se le mandó a Factus — hace falta para poder eliminarla si quedó pendiente
    private String numeroDocumento;    // asignado por Factus
    private String cude;
    private String qrUrl;
    private String urlDocumento;
    private String ambiente;

    private String estado;             // ACEPTADA, RECHAZADA, ERROR
    private String respuestaDian;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    private String creadoPor;
}
