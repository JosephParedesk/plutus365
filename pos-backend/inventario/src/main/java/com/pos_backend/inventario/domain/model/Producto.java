package com.pos_backend.inventario.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Producto {
    private String sku;
    private String nombre;
    private String descripcion;
    private String categoriaId;
    private String categoriaNombre;
    private String proveedorId;
    private String proveedorNombre;
    private Double precioCompra;
    private Double precioVenta;
    private Double gananciaPesos;
    private Double gananciaPorcentaje;
    private Integer stock;
    private Integer stockMinimo;
    private String unidad;
    private String imagenUrl;
    private Boolean activo;
    private String empresaId;

    // GENERAL_19 | REDUCIDO_5 | EXENTO | EXCLUIDO — las 4 categorías de IVA vigentes
    // en Colombia (19% general, 5% reducida, exento con derecho a devolución,
    // excluido sin devolución). Default GENERAL_19 si no se especifica.
    private String tipoIva;
}
