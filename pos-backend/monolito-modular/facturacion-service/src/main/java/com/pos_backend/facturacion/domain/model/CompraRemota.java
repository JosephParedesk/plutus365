package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CompraRemota {
    private Long compraId;
    private String numeroComprobante;
    private String tipoTransaccion;
    private Long proveedorId;
    private String proveedorNombre;
    private String estado;
    private List<ItemRemoto> items;
    private Double totalPagar;
    private String cufeProveedor;   // CUFE de la factura electrónica del proveedor, si se importó por XML
    private Boolean tieneCreditoProveedor;   // true = compra a crédito (requisito de la Resolución 000085/2022 para eventos RADIAN)

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ItemRemoto {
        private String productoSku;
        private String descripcion;
        private Double cantidad;
        private Double valorUnitario;
        private Double descuento;
        private String impuestoCargo;      // texto libre, ej "IVA 19%" — ver NuevaCompraPage.tsx
        private String impuestoRetencion;  // texto libre, ej "Retefuente 3,5% Compras"
    }
}
