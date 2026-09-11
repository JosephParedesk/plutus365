package com.pos_backend.compra.domain.model.gateway;

public interface StockGateway {

    /** Suma el stock comprado; crea el producto automáticamente si el SKU no existe todavía. */
    ResultadoStock registrarCompra(
            String sku, String nombre, Integer cantidad, Double precioCompra,
            Long proveedorId, String proveedorNombre, String unidad, String documentoOrigen, String empresaId);

    /** Compensación: resta lo que se había sumado, si algo más falla después. */
    void revertir(String sku, Integer cantidad, String empresaId);

    record ResultadoStock(boolean creado, String nombreProducto) {}
}
