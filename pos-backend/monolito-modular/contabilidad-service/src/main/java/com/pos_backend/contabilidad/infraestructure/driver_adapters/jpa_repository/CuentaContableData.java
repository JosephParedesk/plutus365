package com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cuentas_contables", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"empresaId", "codigo"})
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CuentaContableData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String empresaId;

    @Column(nullable = false, length = 12)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 15)
    private String nivel;

    @Column(length = 12)
    private String codigoPadre;

    @Column(nullable = false, length = 10)
    private String naturaleza;

    private Boolean esTransaccional;

    @Column(length = 100)
    private String categoria;

    @Column(length = 30)
    private String detalleSaldos;

    private Boolean activa;
    private Boolean personalizada;
}
