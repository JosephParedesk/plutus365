package com.pos_backend.facturacion.infraestructure.driver_adapters.http_client;

import com.pos_backend.facturacion.domain.model.ClienteRemoto;
import com.pos_backend.facturacion.domain.model.gateway.ClienteConsultaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class ClienteConsultaGatewayImpl implements ClienteConsultaGateway {

    private final RestClient restClient;

    public ClienteConsultaGatewayImpl(@Value("${cliente.service.url}") String clienteServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(clienteServiceUrl).build();
    }

    @Override
    public ClienteRemoto buscarCliente(Long clienteId, String empresaId) {
        try {
            return restClient.get()
                    .uri("/buscar/{clienteId}", clienteId)
                    .header("X-Empresa-Id", empresaId)
                    .retrieve()
                    .body(ClienteRemoto.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo consultar el cliente: " + e.getMessage());
        }
    }
}
