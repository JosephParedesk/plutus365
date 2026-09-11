package com.pos_backend.facturacion.domain.model;

/**
 * Eventos RADIAN que Plutus365 puede emitir sobre una factura electrónica
 * recibida de un proveedor (developers.factus.com.co/recepcion-de-documentos
 * /emitir-evento, verificado 2026-09-05). El 034 (Aceptación tácita) NO está
 * acá a propósito: la doc dice explícitamente que ese evento no lo puede
 * emitir el receptor — lo genera Factus/la DIAN sola si pasan 3 días hábiles
 * sin reclamo ni aceptación expresa tras el "recibo del bien/servicio".
 */
public enum EventoRadian {

    ACUSE_RECIBO("030", "Acuse de recibo de Factura Electrónica de Venta"),
    RECLAMO("031", "Reclamo de la Factura Electrónica de Venta"),
    RECIBO_BIEN("032", "Recibo del bien y/o prestación del servicio"),
    ACEPTACION_EXPRESA("033", "Aceptación expresa");

    private final String codigo;
    private final String nombre;

    EventoRadian(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }

    public static EventoRadian porCodigo(String codigo) {
        for (EventoRadian e : values())
            if (e.codigo.equals(codigo)) return e;
        throw new IllegalArgumentException("Evento RADIAN no válido o no emitible manualmente: " + codigo);
    }
}
