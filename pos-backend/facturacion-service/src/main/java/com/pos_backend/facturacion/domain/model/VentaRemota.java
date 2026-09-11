package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VentaRemota {
    private Long ventaId;
    private String numeroVenta;
    private Long clienteId;
    private String clienteNombre;
    private String fecha;              // ISO datetime tal como llega de venta-service
    private String estado;
    private List<ItemRemoto> items;
    private Double subtotal;
    private Double descuentoTotal;
    private Double totalIva;
    private Double total;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ItemRemoto {
        private String sku;
        private String nombreProducto;
        private Integer cantidad;
        private Double precioUnitario;
        private Double descuento;
        private Double valorTotal;
        private String tipoIva;   // GENERAL_19 | REDUCIDO_5 | EXENTO | EXCLUIDO
        private Double valorIva;
    }
}
