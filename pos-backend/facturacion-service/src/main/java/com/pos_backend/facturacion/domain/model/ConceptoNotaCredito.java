package com.pos_backend.facturacion.domain.model;

/**
 * Conceptos de corrección para notas crédito, según el anexo técnico de
 * factura electrónica de venta de la DIAN
 * (cac:DiscrepancyResponse/cbc:ResponseCode).
 *
 * OJO con el código 2 (anulación): si se transmite por el mismo valor de la
 * factura original, la operación comercial queda anulada y normalmente hay que
 * expedir una factura nueva que respalde el negocio.
 */
public enum ConceptoNotaCredito {

    DEVOLUCION_PARCIAL("1", "Devolución parcial de bienes o no aceptación parcial del servicio"),
    ANULACION("2", "Anulación de factura electrónica"),
    REBAJA("3", "Rebaja o descuento parcial o total"),
    AJUSTE_PRECIO("4", "Ajuste de precio"),
    DESCUENTO_PRONTO_PAGO("5", "Descuento comercial por pronto pago"),
    DESCUENTO_VOLUMEN("6", "Descuento comercial por volumen de ventas");

    private final String codigo;
    private final String descripcion;

    ConceptoNotaCredito(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }

    public static ConceptoNotaCredito porCodigo(String codigo) {
        for (ConceptoNotaCredito c : values())
            if (c.codigo.equals(codigo)) return c;
        throw new IllegalArgumentException("Concepto de nota crédito no válido: " + codigo);
    }

    /** El código 2 anula la factura completa. */
    public boolean esAnulacion() { return this == ANULACION; }
}
