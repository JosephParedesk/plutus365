package com.pos_backend.facturacion.domain.model;

/**
 * Motivos para la nota de ajuste a documento soporte, según la tabla de
 * referencia de Factus (developers.factus.com.co/tablas-de-referencia/tablas/
 * #motivos-para-la-generaci%C3%B3n-de-notas-de-ajuste, verificado 2026-09-05).
 * Distintos a los de nota crédito de venta (ConceptoNotaCredito) aunque el 1, 3
 * y 4 coinciden en texto — esta tabla es específica de documento soporte.
 */
public enum ConceptoNotaAjuste {

    DEVOLUCION_PARCIAL("1", "Devolución parcial de los bienes y/o no aceptación parcial del servicio"),
    ANULACION("2", "Anulación del documento soporte en adquisiciones efectuadas a sujetos no obligados a expedir factura de venta o documento equivalente"),
    REBAJA("3", "Rebaja o descuento parcial o total"),
    AJUSTE_PRECIO("4", "Ajuste de precio"),
    OTROS("5", "Otros");

    private final String codigo;
    private final String descripcion;

    ConceptoNotaAjuste(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }

    public static ConceptoNotaAjuste porCodigo(String codigo) {
        for (ConceptoNotaAjuste c : values())
            if (c.codigo.equals(codigo)) return c;
        throw new IllegalArgumentException("Concepto de nota de ajuste no válido: " + codigo);
    }

    public boolean esAnulacion() { return this == ANULACION; }
}
