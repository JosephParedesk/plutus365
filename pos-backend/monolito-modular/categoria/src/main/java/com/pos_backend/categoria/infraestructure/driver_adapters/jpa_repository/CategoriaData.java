package com.pos_backend.categoria.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorias")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CategoriaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoriaId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(length = 50)
    private String icono;

    private Boolean activo;

    // null = categoría base compartida (legado). No-null = privada de esa empresa.
    @Column(length = 50)
    private String empresaId;
}