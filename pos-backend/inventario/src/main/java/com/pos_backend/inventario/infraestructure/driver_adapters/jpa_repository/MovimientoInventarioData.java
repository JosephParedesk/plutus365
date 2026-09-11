package com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "movimientos_inventario",
        indexes = { @Index(name = "idx_mov_sku", columnList = "empresaId,sku,fecha") })
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class MovimientoInventarioData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movimientoId;

    @Column(nullable = false, length = 50) private String empresaId;
    @Column(nullable = false, length = 60) private String sku;
    @Column(length = 150) private String nombreProducto;
    private LocalDateTime fecha;
    @Column(length = 30) private String tipo;
    @Column(length = 60) private String documentoOrigen;
    @Column(length = 255) private String descripcion;
    private Integer cantidad;
    private Integer saldoAnterior;
    private Integer saldoNuevo;
    private Double costoUnitario;
    private Double valorMovimiento;
    @Column(length = 100) private String usuario;
}
