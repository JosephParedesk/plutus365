package com.pos_backend.inventario.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class RegistroCompraStock {
    private String sku;
    private String nombre;           // solo se usa si hay que crear el producto
    private Integer cantidad;
    private Double precioCompra;
    private String proveedorId;
    private String proveedorNombre;
    private String categoriaId;      // opcional
    private String unidad;           // opcional, por defecto "UND"
    private String documentoOrigen;  // número de compra que causó el movimiento
}
