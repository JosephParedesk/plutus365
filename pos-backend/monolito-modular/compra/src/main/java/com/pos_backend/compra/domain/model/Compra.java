package com.pos_backend.compra.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Compra {
    private Long compraId;
    private String empresaId;
    private String tipoTransaccion;   // ver TipoTransaccion en CompraUseCase
    private String numeroComprobante;
    private String facturaProveedor;  // número de factura del proveedor (no el nuestro)
    private String cufeProveedor;     // CUFE de la factura electrónica del proveedor (si aplica)
    private Long proveedorId;
    private String proveedorNombre;
    private LocalDate fechaElaboracion;
    private String creadoPor;
    private String sucursal;
    private Long centroCostoId;
    private String centroCostoNombre;
    private String estado;            // BORRADOR, REGISTRADA, ANULADA
    private Double totalBruto;
    private Double totalDescuentos;
    private Double subtotal;
    private Double totalIva;
    private Double totalRetencion;
    private Double totalPagar;
    private List<CompraItem> items;
    private List<FormaPago> formasPago;

    // Cuentas por pagar (crédito a proveedores)
    private Boolean tieneCreditoProveedor;
    private LocalDate fechaVencimientoCredito;
    private Double saldoPendiente;

    // RECIBO_PAGO: puede pagar varias compras del mismo proveedor a la vez.
    private String tipoRecibo;        // ABONO_DEUDA, ANTICIPO, AVANZADO
    private String origenDinero;      // ej: "Efectivo", "Banco Davivienda"
    private List<AplicacionPago> aplicaciones;

    // NOTA_DEBITO y AJUSTE_CARTERA siguen aplicando sobre una sola compra original.
    private Long compraReferenciaId;
    private String observaciones;
}