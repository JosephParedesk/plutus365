package com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "productos")
@Data
public class ProductoData {


    @Id
    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Relación con categoría (solo guardamos el ID; el nombre se resuelve en el mapper)
    private String categoriaId;

    // Relación con proveedor
    private String proveedorId;

    @Column(nullable = false)
    private Double precioCompra;

    @Column(nullable = false)
    private Double precioVenta;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer stockMinimo;

    @Column(nullable = false)
    private String unidad;

    // URL o path de la imagen almacenada (se guarda después del upload)
    private String imagenUrl;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, length = 50)
    private String empresaId;

    @Column(nullable = false, length = 20)
    private String tipoIva = "GENERAL_19";
}
