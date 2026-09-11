package com.pos_backend.venta.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class VentaItem {
    private Long itemId;
    private String sku;              // referencia a Producto en inventario-service
    private String nombreProducto;
    private Integer cantidad;
    // Precio de venta al público, IVA INCLUIDO (copia de Producto.precioVenta). El
    // desglose de base gravable e IVA se calcula hacia adentro en calcularTotales().
    private Double precioUnitario;
    private Double descuento;
    // Base gravable de la línea (SIN IVA) — cantidad*precioUnitario menos descuento,
    // desglosado hacia adentro. Es lo que se usa en contabilidad y en el XML DIAN
    // (LineExtensionAmount/TaxableAmount deben ir sin impuesto).
    private Double valorTotal;

    // Tarifa de IVA del producto al momento de la venta (copia, como nombreProducto —
    // si después cambia el IVA del producto, esta venta ya facturada no se mueve).
    private String tipoIva;   // GENERAL_19 | REDUCIDO_5 | EXENTO | EXCLUIDO
    private Double valorIva;  // ya calculado: valorTotal + valorIva == cantidad*precioUnitario - descuento
}
