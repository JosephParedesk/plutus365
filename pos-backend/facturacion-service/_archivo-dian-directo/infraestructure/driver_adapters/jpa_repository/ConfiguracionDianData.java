package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_dian")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ConfiguracionDianData {

    @Id
    @Column(length = 50)
    private String empresaId;

    @Column(length = 50)
    private String resolucionNumero;

    @Column(length = 20)
    private String resolucionFechaInicio;

    @Column(length = 20)
    private String resolucionFechaFin;

    @Column(length = 10)
    private String prefijo;

    private Long rangoDesde;
    private Long rangoHasta;
    private Long consecutivoActual;

    @Column(length = 100)
    private String softwareId;

    @Column(length = 100)
    private String softwarePin;

    @Column(length = 100)
    private String claveTecnica;

    @Column(length = 20)
    private String ambiente;

    @Column(length = 100)
    private String testSetId;

    @Column(length = 150)
    private String certificadoNombreArchivo;

    @Column(length = 30)
    private String certificadoCargadoEn;
}
