package com.pos_backend.facturacion.infraestructure.driver_adapters.http_client;

import com.pos_backend.facturacion.domain.model.NominaRemota;
import com.pos_backend.facturacion.domain.model.gateway.NominaConsultaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class NominaConsultaGatewayImpl implements NominaConsultaGateway {

    private final RestClient restClient;

    public NominaConsultaGatewayImpl(@Value("${nomina.service.url}") String nominaServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(nominaServiceUrl).build();
    }

    @Override
    public NominaRemota buscarNomina(Long nominaId, String empresaId) {
        try {
            return restClient.get()
                    .uri("/periodos/{nominaId}", nominaId)
                    .header("X-Empresa-Id", empresaId)
                    .retrieve()
                    .body(NominaRemota.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo consultar la nómina: " + e.getMessage());
        }
    }
}
