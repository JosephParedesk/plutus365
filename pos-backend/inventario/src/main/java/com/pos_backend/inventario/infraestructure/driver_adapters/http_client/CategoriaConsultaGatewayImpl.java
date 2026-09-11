package com.pos_backend.inventario.infraestructure.driver_adapters.http_client;

import com.pos_backend.inventario.domain.model.gateway.CategoriaConsultaGateway;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CategoriaConsultaGatewayImpl implements CategoriaConsultaGateway {

    private final RestClient restClient;

    public CategoriaConsultaGatewayImpl(@Value("${categoria.service.url}") String categoriaServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(categoriaServiceUrl).build();
    }

    @Override
    public String buscarIdPorNombre(String nombre, String empresaId) {
        if (nombre == null || nombre.isBlank()) return null;
        try {
            CategoriaRemota[] categorias = restClient.get()
                    .uri("/listar")
                    .header("X-Empresa-Id", empresaId)
                    .retrieve()
                    .body(CategoriaRemota[].class);
            if (categorias == null) return null;
            for (CategoriaRemota c : categorias)
                if (c.nombre() != null && nombre.trim().equalsIgnoreCase(c.nombre().trim()))
                    return String.valueOf(c.categoriaId());
        } catch (Exception ignored) {
            // si categoria-service no responde, la fila queda sin categoría y sigue el resto de la importación
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CategoriaRemota(Long categoriaId, String nombre) {}
}
