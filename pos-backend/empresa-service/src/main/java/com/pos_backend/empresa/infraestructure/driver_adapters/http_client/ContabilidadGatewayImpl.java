package com.pos_backend.empresa.infraestructure.driver_adapters.http_client;

import com.pos_backend.empresa.domain.model.gateway.ContabilidadGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ContabilidadGatewayImpl implements ContabilidadGateway {

    private static final Logger log = LoggerFactory.getLogger(ContabilidadGatewayImpl.class);

    private final RestClient restClient;

    public ContabilidadGatewayImpl(@Value("${contabilidad.service.url}") String contabilidadServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(contabilidadServiceUrl).build();
    }

    @Override
    public void sembrarPlanDeCuentas(String empresaId) {
        // GET /cuentas siembra las 197 cuentas base la primera vez que se llama
        // para esa empresa (ver CuentaContableUseCase.sembrarBaseSiEsNecesario
        // en contabilidad-service). Nunca debe tumbar la configuración de la
        // empresa si contabilidad-service está caído: la siembra se dispara
        // sola en cuanto alguien liste las cuentas más adelante.
        try {
            restClient.get().header("X-Empresa-Id", empresaId).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo sembrar el plan de cuentas para empresa {} al configurarla: {}", empresaId, e.getMessage());
        }
    }
}
