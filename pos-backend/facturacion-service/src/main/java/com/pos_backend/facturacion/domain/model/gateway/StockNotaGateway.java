package com.pos_backend.facturacion.domain.model.gateway;

public interface StockNotaGateway {
    /** Devuelve al inventario la mercancía acreditada en una nota crédito. */
    void reintegrar(String sku, Integer cantidad, String empresaId);

    /** Compensación: deshace un reintegro si algo falla después. */
    void revertirReintegro(String sku, Integer cantidad, String empresaId);
}
