package com.pos_backend.facturacion.infraestructure.driver_adapters.http_client;

import com.pos_backend.facturacion.domain.model.gateway.StockNotaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class StockNotaGatewayImpl implements StockNotaGateway {

    private final RestClient restClient;

    public StockNotaGatewayImpl(@Value("${inventario.service.url}") String inventarioServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(inventarioServiceUrl).build();
    }

    @Override
    public void reintegrar(String sku, Integer cantidad, String empresaId) {
        restClient.patch()
                .uri("/stock/{sku}/incrementar", sku)
                .header("X-Empresa-Id", empresaId)
                .body(Map.of("cantidad", cantidad))
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void revertirReintegro(String sku, Integer cantidad, String empresaId) {
        // best-effort: si esto falla ya se está propagando un error más importante
        try {
            restClient.patch()
                    .uri("/stock/{sku}/descontar", sku)
                    .header("X-Empresa-Id", empresaId)
                    .body(Map.of("cantidad", cantidad))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) { }
    }
}
