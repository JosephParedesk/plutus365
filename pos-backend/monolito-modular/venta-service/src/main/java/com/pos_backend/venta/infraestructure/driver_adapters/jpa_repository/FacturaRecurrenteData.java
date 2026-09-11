package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "facturas_recurrentes")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FacturaRecurrenteData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recurrenteId;

    @Column(nullable = false, length = 50) private String empresaId;
    @Column(length = 150) private String nombre;
    private Long clienteId;
    @Column(length = 150) private String clienteNombre;
    @Column(length = 120) private String clienteCorreo;

    private Double descuentoTotal;
    private Double totalIva;
    private Double total;

    @Column(length = 20) private String periodicidad;
    private Integer diaGeneracion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate proximaGeneracion;
    private LocalDate ultimaGeneracion;
    private Integer vecesGeneradas;
    private Boolean activa;

    private Long centroCostoId;
    @Column(length = 120) private String centroCostoNombre;
    @Column(length = 30) private String metodoPagoPredeterminado;
    @Column(length = 500) private String observaciones;
    @Column(length = 100) private String creadoPor;

    @Lob @Column(columnDefinition = "TEXT") private String itemsJson;
}
