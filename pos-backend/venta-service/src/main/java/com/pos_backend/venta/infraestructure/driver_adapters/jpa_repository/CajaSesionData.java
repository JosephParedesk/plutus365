package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "caja_sesiones")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CajaSesionData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cajaId;

    @Column(nullable = false, length = 50)
    private String empresaId;

    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    private Double montoApertura;
    private Double montoCierreDeclarado;
    private Double montoCierreCalculado;
    private Double diferencia;

    private Double totalVentasEfectivo;
    private Double totalVentasOtros;
    private Integer numeroVentas;

    @Column(length = 20)
    private String estado;

    @Column(length = 100)
    private String usuarioApertura;

    @Column(length = 100)
    private String usuarioCierre;

    @Column(length = 500)
    private String observaciones;
}
