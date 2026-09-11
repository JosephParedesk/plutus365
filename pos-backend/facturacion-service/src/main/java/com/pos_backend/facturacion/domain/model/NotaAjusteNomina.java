package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Nota de ajuste de nómina electrónica (DIAN) — anula ante la DIAN una nómina
 * electrónica de UN empleado ya ACEPTADA (`/v2/adjustment-payrolls`). A
 * diferencia de nota crédito/débito, esta no lleva ítems ni concepto de
 * corrección: es una eliminación pura, referenciada solo por el número de la
 * nómina electrónica original.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotaAjusteNomina {
    private Long notaAjusteNominaId;
    private String empresaId;

    private Long nominaElectronicaId;      // nómina electrónica que se anula
    private String numeroNominaElectronica; // copia, para mostrar sin joins ("payroll_number")

    private String referenceCode;
    private String numeroAjuste;           // asignado por Factus
    private String ambiente;

    private String estado;                 // ACEPTADA, RECHAZADA, ERROR
    private String respuestaDian;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    private String creadoPor;
}
