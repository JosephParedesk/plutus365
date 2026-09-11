package com.pos_backend.categoria.domain.model;



import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Categoria {
    private Long categoriaId;
    private String nombre;
    private String descripcion;
    private String icono;
    private Boolean activo;

    // null = categoría base compartida (legado, visible para todas las empresas
    // pero de solo lectura). No-null = creada por esa empresa, privada suya.
    private String empresaId;
}