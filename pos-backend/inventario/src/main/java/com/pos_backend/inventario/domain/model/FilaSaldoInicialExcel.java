package com.pos_backend.inventario.domain.model;

import lombok.*;

/** Una fila de la plantilla de saldos iniciales de inventario (cantidad + costo, no precio de venta). */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FilaSaldoInicialExcel {
    private int numeroFila;
    private String sku;
    private Integer cantidad;
    private Double costoUnitario;
}
