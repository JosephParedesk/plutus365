package com.pos_backend.venta.infraestructure.driver_adapters.local_client;

import com.pos_backend.inventario.domain.usecase.ProductoUseCase;
import com.pos_backend.venta.domain.model.gateway.StockGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Reemplaza el http_client original (RestClient a inventario-service): ahora
// que inventario ya está migrado, es una llamada directa al UseCase. El
// UseCase ya lanza RuntimeException/NoSuchElementException con los mismos
// mensajes que antes había que extraer del body HTTP de error.
@Component("ventaStockGatewayImpl")
@RequiredArgsConstructor
public class StockGatewayImpl implements StockGateway {

    private final ProductoUseCase productoUseCase;

    @Override
    public void descontarStock(String sku, String empresaId, Integer cantidad) {
        productoUseCase.descontarStock(sku, empresaId, cantidad);
    }

    @Override
    public void incrementarStock(String sku, String empresaId, Integer cantidad) {
        productoUseCase.incrementarStock(sku, empresaId, cantidad);
    }
}
