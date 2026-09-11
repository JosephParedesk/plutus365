package com.pos_backend.compra.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AplicacionPago {
    private Long compraReferenciaId;
    private Double valorAplicado;
}
