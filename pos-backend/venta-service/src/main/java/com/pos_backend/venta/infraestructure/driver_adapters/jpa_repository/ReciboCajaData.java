package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "recibos_caja")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ReciboCajaData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reciboId;

    @Column(nullable = false, length = 50) private String empresaId;
    @Column(length = 30) private String numeroRecibo;
    private Long clienteId;
    @Column(length = 150) private String clienteNombre;
    private LocalDateTime fecha;
    private LocalDate fechaRecibido;
    @Column(length = 30) private String tipoRecibo;
    @Column(length = 30) private String origenDinero;
    @Column(length = 100) private String bancoDestino;
    @Column(length = 100) private String referenciaPago;
    private Double totalRecibido;
    @Column(length = 30) private String estado;
    @Column(length = 500) private String observaciones;
    @Column(length = 100) private String creadoPor;

    @Lob @Column(columnDefinition = "TEXT") private String aplicacionesJson;
}
