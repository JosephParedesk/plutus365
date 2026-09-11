package com.pos_backend.inventario.infraestructure.driver_adapters.http_client;

import com.pos_backend.inventario.domain.model.gateway.ProveedorConsultaGateway;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProveedorConsultaGatewayImpl implements ProveedorConsultaGateway {

    private final RestClient restClient;

    public ProveedorConsultaGatewayImpl(@Value("${proveedor.service.url}") String proveedorServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(proveedorServiceUrl).build();
    }

    @Override
    public String buscarIdPorNombre(String nombre, String empresaId) {
        if (nombre == null || nombre.isBlank()) return null;
        try {
            ProveedorRemoto[] proveedores = restClient.get()
                    .uri("/listar")
                    .header("X-Empresa-Id", empresaId)
                    .retrieve()
                    .body(ProveedorRemoto[].class);
            if (proveedores == null) return null;
            for (ProveedorRemoto p : proveedores)
                if (p.nombre() != null && nombre.trim().equalsIgnoreCase(p.nombre().trim()))
                    return String.valueOf(p.proveedorId());
        } catch (Exception ignored) {
            // si proveedor-service no responde, la fila queda sin proveedor y sigue el resto de la importación
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProveedorRemoto(Long proveedorId, String nombre) {}
}
