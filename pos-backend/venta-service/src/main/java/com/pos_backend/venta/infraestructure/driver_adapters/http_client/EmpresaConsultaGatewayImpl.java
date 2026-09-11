package com.pos_backend.venta.infraestructure.driver_adapters.http_client;

import com.pos_backend.venta.domain.model.EmpresaRemota;
import com.pos_backend.venta.domain.model.gateway.EmpresaConsultaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class EmpresaConsultaGatewayImpl implements EmpresaConsultaGateway {

    private final RestClient restClient;

    public EmpresaConsultaGatewayImpl(@Value("${empresa.service.url}") String empresaServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(empresaServiceUrl).build();
    }

    @Override
    public EmpresaRemota buscarEmpresa(String empresaId) {
        try {
            return restClient.get()
                    .uri("")
                    .header("X-Empresa-Id", empresaId)
                    .retrieve()
                    .body(EmpresaRemota.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo consultar la empresa: " + e.getMessage());
        }
    }
}
