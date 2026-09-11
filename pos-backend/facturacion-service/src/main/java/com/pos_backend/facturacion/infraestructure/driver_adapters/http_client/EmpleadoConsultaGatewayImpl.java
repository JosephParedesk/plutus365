package com.pos_backend.facturacion.infraestructure.driver_adapters.http_client;

import com.pos_backend.facturacion.domain.model.EmpleadoRemota;
import com.pos_backend.facturacion.domain.model.gateway.EmpleadoConsultaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class EmpleadoConsultaGatewayImpl implements EmpleadoConsultaGateway {

    private final RestClient restClient;

    public EmpleadoConsultaGatewayImpl(@Value("${nomina.service.url}") String nominaServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(nominaServiceUrl).build();
    }

    @Override
    public EmpleadoRemota buscarEmpleado(Long empleadoId, String empresaId) {
        try {
            return restClient.get()
                    .uri("/empleados/{empleadoId}", empleadoId)
                    .header("X-Empresa-Id", empresaId)
                    .retrieve()
                    .body(EmpleadoRemota.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo consultar el empleado: " + e.getMessage());
        }
    }
}
