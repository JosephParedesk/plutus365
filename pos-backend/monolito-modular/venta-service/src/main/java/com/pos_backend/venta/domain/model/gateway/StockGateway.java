package com.pos_backend.venta.domain.model.gateway;

public interface StockGateway {
    /**
     * Descuenta stock en inventario-service. Lanza RuntimeException si el
     * producto no existe o si no hay stock suficiente.
     */
    void descontarStock(String sku, String empresaId, Integer cantidad);

    /**
     * Repone stock en inventario-service. Se usa para anular una venta o
     * para compensar (rollback) items ya descontados cuando otro ítem falla.
     */
    void incrementarStock(String sku, String empresaId, Integer cantidad);
}
