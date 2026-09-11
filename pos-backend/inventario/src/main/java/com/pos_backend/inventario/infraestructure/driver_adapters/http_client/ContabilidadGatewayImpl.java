package com.pos_backend.inventario.infraestructure.driver_adapters.http_client;

import com.pos_backend.inventario.domain.model.gateway.ContabilidadGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class ContabilidadGatewayImpl implements ContabilidadGateway {

    private final RestClient restClient;

    public ContabilidadGatewayImpl(@Value("${contabilidad.service.url}") String contabilidadServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(contabilidadServiceUrl).build();
    }

    @Override
    public String registrarSaldoInicialInventario(double valorTotal, String cuentaContrapartida, LocalDate fechaCorte,
                                                    String empresaId, String creadoPor) {
        Map<String, Object> body = new HashMap<>();
        body.put("valorTotal", valorTotal);
        body.put("cuentaContrapartida", cuentaContrapartida);
        body.put("fechaCorte", (fechaCorte != null ? fechaCorte : LocalDate.now()).format(DateTimeFormatter.ISO_DATE));

        try {
            Map<String, Object> respuesta = restClient.post()
                    .uri("/desde-saldo-inicial-inventario")
                    .header("X-Empresa-Id", empresaId)
                    .header("X-Usuario-Nombre", creadoPor != null ? creadoPor : "Sistema")
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return respuesta != null ? String.valueOf(respuesta.get("numero")) : null;
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(extraerMensaje(e));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo registrar el asiento de saldo inicial: " + e.getMessage());
        }
    }

    private String extraerMensaje(HttpStatusCodeException e) {
        try {
            String cuerpo = e.getResponseBodyAsString();
            int i = cuerpo.indexOf("\"message\":\"");
            if (i >= 0) {
                int fin = cuerpo.indexOf("\"", i + 11);
                return cuerpo.substring(i + 11, fin);
            }
        } catch (Exception ignored) { }
        return "Error de contabilidad (" + e.getStatusCode() + ")";
    }
}
