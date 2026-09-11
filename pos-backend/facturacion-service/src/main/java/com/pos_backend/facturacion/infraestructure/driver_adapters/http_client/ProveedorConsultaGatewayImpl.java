package com.pos_backend.facturacion.infraestructure.driver_adapters.http_client;

import com.pos_backend.facturacion.domain.model.ProveedorRemota;
import com.pos_backend.facturacion.domain.model.gateway.ProveedorConsultaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class ProveedorConsultaGatewayImpl implements ProveedorConsultaGateway {

    private final RestClient restClient;

    public ProveedorConsultaGatewayImpl(@Value("${proveedor.service.url}") String proveedorServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(proveedorServiceUrl).build();
    }

    @Override
    public ProveedorRemota buscarProveedor(Long proveedorId, String empresaId) {
        try {
            return restClient.get()
                    .uri("/buscar/{proveedorId}", proveedorId)
                    .header("X-Empresa-Id", empresaId)
                    .retrieve()
                    .body(ProveedorRemota.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo consultar el proveedor: " + e.getMessage());
        }
    }
}
