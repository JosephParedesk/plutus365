package com.pos_backend.contabilidad.domain.model;

import lombok.*;

/**
 * Centro de costo: permite segmentar ingresos y gastos por área, sucursal,
 * proyecto o línea de negocio, para saber cuál es rentable y cuál no.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CentroCosto {
    private Long centroCostoId;
    private String empresaId;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String responsable;
    private Boolean activo;
}
