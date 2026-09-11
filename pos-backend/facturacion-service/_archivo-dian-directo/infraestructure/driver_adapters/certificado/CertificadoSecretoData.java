package com.pos_backend.facturacion.infraestructure.driver_adapters.certificado;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "certificado_secretos")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CertificadoSecretoData {

    @Id
    @Column(length = 50)
    private String empresaId;

    @Column(length = 150)
    private String nombreArchivo;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String passwordEncriptado;

    @Column(length = 30)
    private String cargadoEn;
}
