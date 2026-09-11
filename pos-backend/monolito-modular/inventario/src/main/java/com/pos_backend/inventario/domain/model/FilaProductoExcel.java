package com.pos_backend.inventario.domain.model;

import lombok.*;

/** Una fila de la plantilla de importación de catálogo (sin cantidades — eso es aparte, ver saldos iniciales). */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FilaProductoExcel {
    private int numeroFila;          // para reportar errores con contexto (empieza en 2: la 1 es el encabezado)
    private String sku;
    private String nombre;
    private String descripcion;
    private String categoriaNombre;  // opcional, se resuelve por nombre
    private String proveedorNombre;  // opcional, se resuelve por nombre
    private Double precioCompra;
    private Double precioVenta;
    private Integer stockMinimo;
    private String unidad;
    private String tipoIva;          // GENERAL_19 | REDUCIDO_5 | EXENTO | EXCLUIDO (opcional, default GENERAL_19)
}
