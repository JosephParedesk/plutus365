package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "cotizaciones")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CotizacionData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cotizacionId;

    @Column(nullable = false, length = 50) private String empresaId;
    @Column(length = 30) private String numeroCotizacion;
    private Long clienteId;
    @Column(length = 150) private String clienteNombre;
    @Column(length = 120) private String clienteCorreo;
    private LocalDateTime fecha;
    private LocalDate fechaVencimiento;
    @Column(length = 20) private String estado;

    private Double subtotal;
    private Double descuentoTotal;
    private Double totalIva;
    private Double total;

    private Long centroCostoId;
    @Column(length = 120) private String centroCostoNombre;

    @Column(length = 120) private String lugarEmision;
    @Column(length = 150) private String contactoNombre;
    @Column(length = 100) private String contactoCargo;

    @Column(length = 255) private String formaPago;
    @Column(length = 255) private String tiempoEntrega;
    @Column(length = 255) private String lugarEntrega;
    @Column(length = 255) private String transporte;
    @Column(length = 255) private String tiempoFabricacion;
    @Column(length = 255) private String instalacion;
    @Column(length = 255) private String capacitacion;

    @Column(length = 120) private String garantiaTiempo;
    @Column(length = 500) private String garantiaCubre;
    @Column(length = 500) private String garantiaNoCubre;
    @Column(length = 500) private String garantiaComoHacerEfectiva;

    @Column(length = 1000) private String observaciones;

    @Column(length = 150) private String asesorNombre;
    @Column(length = 100) private String asesorCargo;
    @Column(length = 40) private String asesorTelefono;
    @Column(length = 120) private String asesorCorreo;

    private Long ventaGeneradaId;
    @Column(length = 100) private String creadoPor;

    @Lob @Column(columnDefinition = "TEXT") private String itemsJson;
}
