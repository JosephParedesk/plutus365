package com.pos_backend.facturacion.infraestructure.driver_adapters.local_client;

import com.pos_backend.facturacion.domain.model.gateway.StockNotaGateway;
import com.pos_backend.inventario.domain.usecase.ProductoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Reemplaza el http_client original (RestClient a inventario-service): ahora
// que inventario ya está migrado, es una llamada directa al UseCase.
@Component("facturacionStockNotaGatewayImpl")
@RequiredArgsConstructor
public class StockNotaGatewayImpl implements StockNotaGateway {

    private final ProductoUseCase productoUseCase;

    @Override
    public void reintegrar(String sku, Integer cantidad, String empresaId) {
        productoUseCase.incrementarStock(sku, empresaId, cantidad);
    }

    @Override
    public void revertirReintegro(String sku, Integer cantidad, String empresaId) {
        try {
            productoUseCase.descontarStock(sku, empresaId, cantidad);
        } catch (Exception ignored) {
            // best-effort: si esto falla ya se está propagando un error más importante
        }
    }
}
