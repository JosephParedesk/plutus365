package com.pos_backend.proveedor.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;

// El NIT y el nombre ya no son únicos globalmente: el mismo proveedor del mundo
// real (mismo NIT) puede ser proveedor de varias empresas distintas en este SaaS.
// Únicos por (empresa_id, nit) y (empresa_id, nombre) — ver ALTER TABLE aplicado
// a mano, ddl-auto=update no reescribe constraints de columnas existentes.
@Entity
@Table(name = "proveedores", uniqueConstraints = {
        @UniqueConstraint(name = "uq_proveedores_empresa_nit", columnNames = {"empresa_id", "nit"}),
        @UniqueConstraint(name = "uq_proveedores_empresa_nombre", columnNames = {"empresa_id", "nombre"})
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ProveedorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long proveedorId;

    @Column(nullable = false, length = 20)
    private String nit;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 100)
    private String contacto;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 150)
    private String correo;

    @Column(length = 255)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 50)
    private String plazoCredito;

    private Boolean activo;

    // null = proveedor base compartido (legado). No-null = privado de esa empresa.
    @Column(length = 50)
    private String empresaId;
}
