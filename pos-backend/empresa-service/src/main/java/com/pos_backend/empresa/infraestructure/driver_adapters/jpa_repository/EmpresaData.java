package com.pos_backend.empresa.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresas")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class EmpresaData {

    @Id
    @Column(length = 50)
    private String empresaId;

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

    @Column(length = 20)
    private String periodicidadIva;

    @Column(nullable = false)
    private Boolean agenteRetenedor = false;

    @Column(length = 200)
    private String razonSocial;

    @Column(length = 100)
    private String nombres;

    @Column(length = 100)
    private String apellidos;

    @Column(length = 150)
    private String nombreComercial;

    @Column(length = 150)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String departamento;

    @Column(length = 100)
    private String pais;

    @Column(length = 255)
    private String logoUrl;

    @Column(length = 10)
    private String colorPrincipal;

    @Column(length = 10)
    private String moneda;

    @Column(nullable = false)
    private Boolean posHabilitado = false;
}
