package com.pos_backend.compra.infraestructure.driver_adapters.http_client;

import com.pos_backend.compra.domain.model.gateway.StockGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class StockGatewayImpl implements StockGateway {

    private final RestClient restClient;

    public StockGatewayImpl(@Value("${inventario.service.url}") String inventarioServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(inventarioServiceUrl).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResultadoStock registrarCompra(
            String sku, String nombre, Integer cantidad, Double precioCompra,
            Long proveedorId, String proveedorNombre, String unidad, String documentoOrigen, String empresaId) {

        Map<String, Object> body = new HashMap<>();
        body.put("sku", sku);
        body.put("nombre", nombre);
        body.put("cantidad", cantidad);
        body.put("precioCompra", precioCompra);
        body.put("proveedorId", proveedorId != null ? String.valueOf(proveedorId) : null);
        body.put("proveedorNombre", proveedorNombre);
        body.put("unidad", unidad);
        body.put("documentoOrigen", documentoOrigen);

        try {
            Map<String, Object> respuesta = restClient.post()
                    .uri("/stock/registrar-compra")
                    .header("X-Empresa-Id", empresaId)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            boolean creado = respuesta != null && Boolean.TRUE.equals(respuesta.get("creado"));
            Map<String, Object> producto = respuesta != null ? (Map<String, Object>) respuesta.get("producto") : null;
            String nombreProducto = producto != null && producto.get("nombre") != null ? (String) producto.get("nombre") : nombre;
            return new ResultadoStock(creado, nombreProducto);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(extraerMensaje(e));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo actualizar el inventario: " + e.getMessage());
        }
    }

    @Override
    public void revertir(String sku, Integer cantidad, String empresaId) {
        try {
            restClient.patch()
                    .uri("/stock/{sku}/descontar", sku)
                    .header("X-Empresa-Id", empresaId)
                    .body(Map.of("cantidad", cantidad))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {
            // best-effort: si esto falla, ya se está propagando una excepción más importante que esta
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
        return "Error de inventario (" + e.getStatusCode() + ")";
    }
}
