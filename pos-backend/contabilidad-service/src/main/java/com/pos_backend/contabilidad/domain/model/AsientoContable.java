package com.pos_backend.contabilidad.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AsientoContable {
    private Long asientoId;
    private String empresaId;
    private String numero;          // AS-00001

    private LocalDate fecha;
    private String descripcion;

    private String origen;          // VENTA, COMPRA, MANUAL
    private Long referenciaId;      // ventaId o compraId (null si es MANUAL)

    private String estado;          // CONTABILIZADO, ANULADO

    private List<MovimientoContable> movimientos;
    private Double totalDebe;
    private Double totalHaber;

    private String creadoPor;
}
