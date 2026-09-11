package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notas_debito_venta")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NotaDebitoVentaData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notaDebitoId;

    @Column(nullable = false, length = 50) private String empresaId;
    @Column(length = 30) private String numeroNotaDebito;

    private Long ventaReferenciaId;
    @Column(length = 30) private String numeroVentaReferencia;
    private Long clienteId;
    @Column(length = 150) private String clienteNombre;

    private LocalDateTime fecha;
    @Column(length = 255) private String concepto;
    private Double valor;

    @Column(length = 20) private String estado;
    @Column(length = 500) private String observaciones;
    @Column(length = 100) private String creadoPor;
}
