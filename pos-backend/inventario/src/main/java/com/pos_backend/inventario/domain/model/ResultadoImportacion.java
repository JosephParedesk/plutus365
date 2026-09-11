package com.pos_backend.inventario.domain.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResultadoImportacion {
    private int creados;
    private int actualizados;
    private List<ErrorFila> errores = new ArrayList<>();

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ErrorFila {
        private int fila;
        private String sku;
        private String motivo;
    }
}
