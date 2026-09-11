package com.pos_backend.venta.infraestructure.driver_adapters.http_client;

import com.pos_backend.venta.domain.model.FormaPago;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.gateway.ContabilidadGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class ContabilidadGatewayImpl implements ContabilidadGateway {

    private final RestClient restClient;

    public ContabilidadGatewayImpl(@Value("${contabilidad.service.url}") String contabilidadServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(contabilidadServiceUrl).build();
    }

    @Override
    public void generarAsientoVenta(Venta venta, String empresaId) {
        Map<String, Object> body = Map.of(
                "ventaId", venta.getVentaId(),
                "numeroVenta", venta.getNumeroVenta(),
                "fecha", venta.getFecha().toLocalDate().format(DateTimeFormatter.ISO_DATE),
                "clienteId", venta.getClienteId() != null ? venta.getClienteId() : "",
                "subtotal", venta.getSubtotal(),
                "descuentoTotal", venta.getDescuentoTotal() != null ? venta.getDescuentoTotal() : 0,
                "totalIva", venta.getTotalIva() != null ? venta.getTotalIva() : 0,
                "total", venta.getTotal(),
                "formasPago", formasPagoBody(venta.getFormasPago())
        );

        try {
            restClient.post()
                    .uri("/desde-venta")
                    .header("X-Empresa-Id", empresaId)
                    .header("X-Usuario-Nombre", venta.getCreadoPor() != null ? venta.getCreadoPor() : "Sistema")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(extraerMensaje(e));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo contabilizar la venta: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> formasPagoBody(List<FormaPago> formasPago) {
        return formasPago.stream()
                .map(fp -> Map.<String, Object>of("metodo", fp.getMetodo(), "valor", fp.getValor()))
                .toList();
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
