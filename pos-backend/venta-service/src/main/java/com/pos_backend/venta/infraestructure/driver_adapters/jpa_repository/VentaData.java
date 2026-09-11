package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas", uniqueConstraints = @UniqueConstraint(
        name = "uq_ventas_empresa_numero", columnNames = {"empresa_id", "numero_venta"}))
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class VentaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ventaId;

    @Column(nullable = false, length = 50)
    private String empresaId;

    @Column(length = 30)
    private String numeroVenta;

    private Long clienteId;

    @Column(length = 150)
    private String clienteNombre;

    private LocalDateTime fecha;

    @Column(length = 30)
    private String estado;

    private Boolean tieneCreditoCliente;
    private java.time.LocalDate fechaVencimientoCredito;
    private Double saldoPendiente;

    private Long centroCostoId;

    @Column(length = 120)
    private String centroCostoNombre;

    private Boolean esObsequio;

    @Column(length = 100)
    private String creadoPor;

    private Double subtotal;
    private Double descuentoTotal;
    private Double totalIva;
    private Double total;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String formasPagoJson;
}
