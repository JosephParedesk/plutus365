package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "remisiones")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class RemisionData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long remisionId;

    @Column(nullable = false, length = 50) private String empresaId;
    @Column(length = 30) private String numeroRemision;
    private Long clienteId;
    @Column(length = 150) private String clienteNombre;
    private LocalDateTime fecha;
    @Column(length = 20) private String estado;

    private Double subtotal;
    private Double descuentoTotal;
    private Double totalIva;
    private Double total;

    private Long centroCostoId;
    @Column(length = 120) private String centroCostoNombre;

    @Column(length = 255) private String lugarEntrega;
    @Column(length = 150) private String transportador;
    @Column(length = 1000) private String observaciones;

    private Long ventaGeneradaId;
    @Column(length = 100) private String creadoPor;

    @Lob @Column(columnDefinition = "TEXT") private String itemsJson;
}
