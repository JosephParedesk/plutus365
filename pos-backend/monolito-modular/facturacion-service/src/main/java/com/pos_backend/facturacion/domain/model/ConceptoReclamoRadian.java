package com.pos_backend.facturacion.domain.model;

/**
 * Motivo obligatorio cuando el evento RADIAN emitido es RECLAMO (031) —
 * developers.factus.com.co/tablas-de-referencia/tablas/#c%C3%B3digos-de
 * -conceptos-de-reclamo, verificado 2026-09-05.
 */
public enum ConceptoReclamoRadian {

    DOCUMENTO_CON_INCONSISTENCIAS("01", "Documento con inconsistencias"),
    MERCANCIA_NO_ENTREGADA_TOTAL("02", "Mercancía no entregada totalmente"),
    MERCANCIA_NO_ENTREGADA_PARCIAL("03", "Mercancía no entregada parcialmente"),
    SERVICIO_NO_PRESTADO("04", "Servicio no prestado");

    private final String codigo;
    private final String descripcion;

    ConceptoReclamoRadian(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }

    public static ConceptoReclamoRadian porCodigo(String codigo) {
        for (ConceptoReclamoRadian c : values())
            if (c.codigo.equals(codigo)) return c;
        throw new IllegalArgumentException("Concepto de reclamo no válido: " + codigo);
    }
}
