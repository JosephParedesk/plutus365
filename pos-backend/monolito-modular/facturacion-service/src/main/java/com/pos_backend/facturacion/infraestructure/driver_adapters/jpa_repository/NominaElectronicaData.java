package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "nominas_electronicas")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NominaElectronicaData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nominaElectronicaId;

    @Column(nullable = false, length = 50) private String empresaId;
    private Long nominaId;
    private Long empleadoId;
    @Column(length = 200) private String nombreEmpleado;
    private Integer anio;
    private Integer mes;
    @Column(length = 100) private String referenceCode;
    @Column(length = 40) private String numeroDocumento;
    @Column(length = 200) private String cude;
    @Column(length = 500) private String qrUrl;
    @Column(length = 500) private String urlDocumento;
    @Column(length = 20) private String ambiente;
    @Column(length = 20) private String estado;
    @Column(length = 1000) private String respuestaDian;

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
    @Column(length = 100) private String creadoPor;
}
