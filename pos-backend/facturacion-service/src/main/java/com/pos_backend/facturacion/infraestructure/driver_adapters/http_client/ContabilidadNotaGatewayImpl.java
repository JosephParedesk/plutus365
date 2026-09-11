package com.pos_backend.facturacion.infraestructure.driver_adapters.http_client;

import com.pos_backend.facturacion.domain.model.NotaCredito;
import com.pos_backend.facturacion.domain.model.gateway.ContabilidadNotaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class ContabilidadNotaGatewayImpl implements ContabilidadNotaGateway {

    private final RestClient restClient;

    public ContabilidadNotaGatewayImpl(@Value("${contabilidad.service.url}") String contabilidadServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(contabilidadServiceUrl).build();
    }

    @Override
    public void generarAsiento(NotaCredito nota, String metodoPagoOriginal, String empresaId) {
        Map<String, Object> body = new HashMap<>();
        body.put("notaCreditoId", nota.getNotaCreditoId());
        body.put("numeroNota", nota.getNumeroNota());
        body.put("fecha", nota.getFechaEmision() != null
                ? nota.getFechaEmision().toLocalDate().format(DateTimeFormatter.ISO_DATE) : null);
        body.put("subtotal", nota.getSubtotal());
        body.put("totalIva", nota.getTotalIva());
        body.put("total", nota.getTotal());
        body.put("metodoPagoOriginal", metodoPagoOriginal);

        try {
            restClient.post()
                    .uri("/desde-nota-credito")
                    .header("X-Empresa-Id", empresaId)
                    .header("X-Usuario-Nombre", nota.getCreadoPor() != null ? nota.getCreadoPor() : "Sistema")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(extraerMensaje(e));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo contabilizar la nota crédito: " + e.getMessage());
        }
    }

    private String extraerMensaje(HttpStatusCodeException e) {
        try {
            String cuerpo = e.getResponseBodyAsString();
            int i = cuerpo.indexOf("\"message\":\"");
            if (i >= 0) return cuerpo.substring(i + 11, cuerpo.indexOf("\"", i + 11));
        } catch (Exception ignored) { }
        return "Error de contabilidad (" + e.getStatusCode() + ")";
    }
}
