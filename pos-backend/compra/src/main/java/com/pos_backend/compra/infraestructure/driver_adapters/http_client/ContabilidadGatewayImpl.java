package com.pos_backend.compra.infraestructure.driver_adapters.http_client;

import com.pos_backend.compra.domain.model.Compra;
import com.pos_backend.compra.domain.model.CompraItem;
import com.pos_backend.compra.domain.model.FormaPago;
import com.pos_backend.compra.domain.model.gateway.ContabilidadGateway;
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
    public void generarAsientoCompra(Compra compra, String empresaId) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("compraId", compra.getCompraId());
        body.put("tipoTransaccion", compra.getTipoTransaccion());
        body.put("numeroComprobante", compra.getNumeroComprobante());
        body.put("fecha", compra.getFechaElaboracion().format(DateTimeFormatter.ISO_DATE));
        body.put("proveedorId", compra.getProveedorId());
        body.put("totalPagar", compra.getTotalPagar());
        body.put("totalIva", compra.getTotalIva() != null ? compra.getTotalIva() : 0);
        body.put("tieneCreditoProveedor", Boolean.TRUE.equals(compra.getTieneCreditoProveedor()));
        body.put("compraReferenciaId", compra.getCompraReferenciaId()); // puede ser null, HashMap lo admite
        body.put("items", itemsBody(compra.getItems()));
        body.put("formasPago", formasPagoBody(compra));

        try {
            restClient.post()
                    .uri("/desde-compra")
                    .header("X-Empresa-Id", empresaId)
                    .header("X-Usuario-Nombre", compra.getCreadoPor() != null ? compra.getCreadoPor() : "Sistema")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(extraerMensaje(e));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo contabilizar la compra: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> itemsBody(List<CompraItem> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(i -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("tipo", i.getTipo() != null ? i.getTipo() : "GASTO_CUENTA");
                    m.put("descripcion", i.getDescripcion() != null ? i.getDescripcion() : "");
                    m.put("valorTotal", i.getValorTotal() != null ? i.getValorTotal() : 0);
                    m.put("cuentaContableCodigo", i.getCuentaContableCodigo()); // puede ser null
                    return m;
                })
                .toList();
    }

    // RECIBO_PAGO no usa formasPago (usa origenDinero, texto libre); se traduce
    // aquí a una forma de pago sintética para que contabilidad-service decida la cuenta.
    private List<Map<String, Object>> formasPagoBody(Compra compra) {
        if (compra.getFormasPago() != null && !compra.getFormasPago().isEmpty()) {
            return compra.getFormasPago().stream()
                    .map(fp -> Map.<String, Object>of("metodo", fp.getMetodo(), "valor", fp.getValor()))
                    .toList();
        }
        if (compra.getOrigenDinero() != null) {
            String metodo = compra.getOrigenDinero().toLowerCase().contains("efectivo") ? "EFECTIVO" : "TRANSFERENCIA";
            return List.of(Map.of("metodo", metodo, "valor", compra.getTotalPagar()));
        }
        return List.of();
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
