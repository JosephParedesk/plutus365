package com.pos_backend.venta.infraestructure.driver_adapters.http_client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.venta.domain.model.gateway.StockGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@Component
public class StockGatewayImpl implements StockGateway {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StockGatewayImpl(@Value("${inventario.service.url}") String inventarioServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(inventarioServiceUrl).build();
    }

    @Override
    public void descontarStock(String sku, String empresaId, Integer cantidad) {
        ajustarStock("descontar", sku, empresaId, cantidad);
    }

    @Override
    public void incrementarStock(String sku, String empresaId, Integer cantidad) {
        ajustarStock("incrementar", sku, empresaId, cantidad);
    }

    private void ajustarStock(String accion, String sku, String empresaId, Integer cantidad) {
        try {
            restClient.patch()
                    .uri("/stock/{sku}/{accion}", sku, accion)
                    .header("X-Empresa-Id", empresaId)
                    .body(Map.of("cantidad", cantidad))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(extraerMensaje(e, sku));
        } catch (ResourceAccessException e) {
            throw new RuntimeException("inventario-service no está disponible");
        }
    }

    private String extraerMensaje(HttpStatusCodeException e, String sku) {
        try {
            JsonNode nodo = objectMapper.readTree(e.getResponseBodyAsString());
            if (nodo.has("message"))
                return nodo.get("message").asText();
        } catch (Exception ignored) {
            // Si el cuerpo no es JSON, se usa el mensaje genérico de abajo.
        }
        return "Error de inventario para " + sku + " (" + e.getStatusCode() + ")";
    }
}
