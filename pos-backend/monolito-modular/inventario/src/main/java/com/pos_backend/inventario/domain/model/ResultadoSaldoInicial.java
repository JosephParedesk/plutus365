package com.pos_backend.inventario.domain.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResultadoSaldoInicial {
    private int productosActualizados;
    private double valorTotal;
    private String numeroAsiento;
    private List<ResultadoImportacion.ErrorFila> errores = new ArrayList<>();
}
