package com.pos_backend.facturacion.domain.model;

/**
 * Conceptos de corrección para notas débito, según la tabla de referencia de
 * Factus (developers.factus.com.co/tablas-de-referencia/tablas/#codigos-de-correccion-notas-debito) —
 * distintos a los de nota crédito (ConceptoNotaCredito).
 */
public enum ConceptoNotaDebito {

    INTERESES("1", "Intereses"),
    GASTOS_POR_COBRAR("2", "Gastos por cobrar"),
    CAMBIO_VALOR("3", "Cambio del valor"),
    OTROS("4", "Otros");

    private final String codigo;
    private final String descripcion;

    ConceptoNotaDebito(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }

    public static ConceptoNotaDebito porCodigo(String codigo) {
        for (ConceptoNotaDebito c : values())
            if (c.codigo.equals(codigo)) return c;
        throw new IllegalArgumentException("Concepto de nota débito no válido: " + codigo);
    }
}
