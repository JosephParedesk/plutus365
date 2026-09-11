package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_recibo")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ConfiguracionReciboData {

    @Id
    @Column(length = 50)
    private String empresaId;

    @Column(length = 10)
    private String anchoPapel;

    @Column(length = 10)
    private String tamanioFuente;

    @Column(length = 200)
    private String mensajePie;

    private Boolean mostrarLogo;
    private Boolean mostrarDireccionEmpresa;
    private Boolean mostrarAtendidoPor;
}
