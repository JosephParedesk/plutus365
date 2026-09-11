package com.pos_backend.venta.infraestructure.driver_adapters.http_client;

import com.pos_backend.venta.domain.model.ReciboCaja;
import com.pos_backend.venta.domain.model.gateway.ReciboContabilidadGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class ReciboContabilidadGatewayImpl implements ReciboContabilidadGateway {

    private final RestClient restClient;

    public ReciboContabilidadGatewayImpl(@Value("${contabilidad.service.url}") String url) {
        this.restClient = RestClient.builder().baseUrl(url).build();
    }

    @Override
    public void generarAsientoRecibo(ReciboCaja recibo, String empresaId) {
        Map<String, Object> body = new HashMap<>();
        body.put("reciboId", recibo.getReciboId());
        body.put("numeroRecibo", recibo.getNumeroRecibo());
        body.put("fecha", recibo.getFechaRecibido() != null
                ? recibo.getFechaRecibido().format(DateTimeFormatter.ISO_DATE) : null);
        body.put("total", recibo.getTotalRecibido());
        body.put("origenDinero", recibo.getOrigenDinero());
        body.put("esAnticipo", "ANTICIPO".equals(recibo.getTipoRecibo()));

        try {
            restClient.post()
                    .uri("/desde-recibo-caja")
                    .header("X-Empresa-Id", empresaId)
                    .header("X-Usuario-Nombre", recibo.getCreadoPor() != null ? recibo.getCreadoPor() : "Sistema")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(extraerMensaje(e));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo contabilizar el recibo: " + e.getMessage());
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
