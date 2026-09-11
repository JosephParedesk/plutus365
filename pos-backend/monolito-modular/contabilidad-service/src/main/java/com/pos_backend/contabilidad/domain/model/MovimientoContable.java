package com.pos_backend.contabilidad.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class MovimientoContable {
    private String cuentaCodigo;
    private String cuentaNombre;   // se guarda una copia para no depender de joins al listar
    private Double debe;
    private Double haber;
    private String descripcion;
}
