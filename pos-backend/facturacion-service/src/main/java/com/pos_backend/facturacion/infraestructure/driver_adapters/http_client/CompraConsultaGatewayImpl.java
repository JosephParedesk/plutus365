package com.pos_backend.facturacion.infraestructure.driver_adapters.http_client;

import com.pos_backend.facturacion.domain.model.CompraRemota;
import com.pos_backend.facturacion.domain.model.gateway.CompraConsultaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class CompraConsultaGatewayImpl implements CompraConsultaGateway {

    private final RestClient restClient;

    public CompraConsultaGatewayImpl(@Value("${compra.service.url}") String compraServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(compraServiceUrl).build();
    }

    @Override
    public CompraRemota buscarCompra(Long compraId, String empresaId) {
        try {
            return restClient.get()
                    .uri("/buscar/{compraId}", compraId)
                    .header("X-Empresa-Id", empresaId)
                    .retrieve()
                    .body(CompraRemota.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo consultar la compra: " + e.getMessage());
        }
    }
}
