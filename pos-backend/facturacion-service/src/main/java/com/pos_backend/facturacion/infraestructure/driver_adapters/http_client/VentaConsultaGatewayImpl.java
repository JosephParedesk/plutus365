package com.pos_backend.facturacion.infraestructure.driver_adapters.http_client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pos_backend.facturacion.domain.model.VentaRemota;
import com.pos_backend.facturacion.domain.model.gateway.VentaConsultaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class VentaConsultaGatewayImpl implements VentaConsultaGateway {

    private final RestClient restClient;

    public VentaConsultaGatewayImpl(@Value("${venta.service.url}") String ventaServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(ventaServiceUrl).build();
    }

    @Override
    public VentaRemota buscarVenta(Long ventaId, String empresaId) {
        try {
            return restClient.get()
                    .uri("/buscar/{ventaId}", ventaId)
                    .header("X-Empresa-Id", empresaId)
                    .retrieve()
                    .body(VentaRemota.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo consultar la venta: " + e.getMessage());
        }
    }
}
