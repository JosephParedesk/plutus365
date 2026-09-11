package com.pos_backend.cliente.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"numeroDocumento", "empresaId"})
})
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ClienteData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clienteId;

    @Column(nullable = false, length = 20)
    private String tipoPersona;

    @Column(nullable = false, length = 20)
    private String tipoDocumento;

    @Column(nullable = false, length = 20)
    private String numeroDocumento;

    @Column(length = 5)
    private String dv;

    @Column(length = 30)
    private String regimenFiscal;

    @Column(length = 100)
    private String nombres;

    @Column(length = 100)
    private String apellidos;

    @Column(length = 200)
    private String razonSocial;

    @Column(nullable = false, length = 150)
    private String correo;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String departamento;

    @Column(length = 100)
    private String pais;

    @Column(length = 20)
    private String codigoPostal;

    private Boolean activo;

    @Column(nullable = false, length = 50)
    private String empresaId;
}
