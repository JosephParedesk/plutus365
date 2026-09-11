package com.pos_backend.compra.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "compras")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CompraData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long compraId;

    @Column(nullable = false, length = 50)
    private String empresaId;

    @Column(nullable = false, length = 30)
    private String tipoTransaccion;

    @Column(length = 30)
    private String numeroComprobante;

    @Column(length = 50)
    private String facturaProveedor;

    @Column(length = 200)
    private String cufeProveedor;

    private Long centroCostoId;

    @Column(length = 120)
    private String centroCostoNombre;

    @Column(nullable = false)
    private Long proveedorId;

    @Column(length = 150)
    private String proveedorNombre;

    private LocalDate fechaElaboracion;

    @Column(length = 100)
    private String creadoPor;

    @Column(length = 100)
    private String sucursal;

    @Column(length = 30)
    private String estado;

    private Double totalBruto;
    private Double totalDescuentos;
    private Double subtotal;
    private Double totalIva;
    private Double totalRetencion;
    private Double totalPagar;

    private Boolean tieneCreditoProveedor;
    private LocalDate fechaVencimientoCredito;
    private Double saldoPendiente;
    private Long compraReferenciaId;

    @Column(length = 30)
    private String tipoRecibo;

    @Column(length = 100)
    private String origenDinero;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String aplicacionesJson;

    @Column(length = 500)
    private String observaciones;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String formasPagoJson;
}