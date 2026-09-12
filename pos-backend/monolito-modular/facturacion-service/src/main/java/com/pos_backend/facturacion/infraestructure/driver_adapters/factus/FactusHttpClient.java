package com.pos_backend.facturacion.infraestructure.driver_adapters.factus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cliente HTTP de la API de Factus (proveedor tecnológico habilitado ante la DIAN).
 * Auth OAuth2 (grant_type=password) — token cacheado en memoria por empresa, se
 * renueva solo cuando falta o está por expirar. No implementa el flujo de
 * refresh_token: reautenticar con las mismas 4 credenciales es igual de simple.
 */
@Component
public class FactusHttpClient {

    // RestClient.create() a secas dejaba que Spring Boot 4 eligiera su
    // convertidor JSON por autoconfiguración, y desde que el monolito junta
    // spring-boot-starter-web (Jackson 3, tools.jackson.*) con el
    // jackson-databind clásico que usa este archivo (com.fasterxml.jackson.*
    // — necesario porque la API de Factus se modela con JsonNode clásico),
    // a veces elegía el conversor de Jackson 3 y tiraba "Type definition
    // error" al no reconocer com.fasterxml.jackson.databind.JsonNode como
    // uno de sus propios tipos. Se fuerza acá el conversor clásico para este
    // cliente en particular, sin tocar la configuración global de Jackson
    // del resto de la app.
    private final RestClient restClient = RestClient.builder()
            .messageConverters(converters -> converters.add(0, new MappingJackson2HttpMessageConverter(new ObjectMapper())))
            .build();
    private final String baseUrl;
    private final Map<String, TokenCache> tokensPorEmpresa = new ConcurrentHashMap<>();

    public FactusHttpClient(@Value("${factus.base.url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private record TokenCache(String accessToken, Instant expiraEn) {}

    /** Pide un token nuevo (sin cachear) solo para confirmar que las credenciales sirven. */
    public void verificarCredenciales(ConfiguracionDian config) {
        tokensPorEmpresa.remove(config.getEmpresaId());
        obtenerToken(config);
    }

    /** DELETE de un documento NO validado por su reference_code — libera el reintento. */
    public void eliminarDocumento(ConfiguracionDian config, String path) {
        String token = obtenerToken(config);
        try {
            restClient.delete()
                    .uri(baseUrl + path)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new RuntimeException(mensajeError(e));
        }
    }

    /** GET simple con Bearer token — se usa para consultas de solo lectura (ej: rangos de numeración). */
    public JsonNode getDocumento(ConfiguracionDian config, String path) {
        String token = obtenerToken(config);
        try {
            return restClient.get()
                    .uri(baseUrl + path)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw new RuntimeException(mensajeError(e));
        }
    }

    /** PATCH — hoy solo lo usan los eventos RADIAN sobre facturas recibidas. */
    public JsonNode patchDocumento(ConfiguracionDian config, String path, Map<String, Object> body) {
        String token = obtenerToken(config);
        try {
            return restClient.patch()
                    .uri(baseUrl + path)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw new RuntimeException(mensajeError(e));
        }
    }

    /** POST a un endpoint de documentos (bills/validate, credit-notes/validate, etc). */
    public JsonNode postDocumento(ConfiguracionDian config, String path, Map<String, Object> body) {
        String token = obtenerToken(config);
        try {
            return restClient.post()
                    .uri(baseUrl + path)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw new RuntimeException(mensajeError(e));
        }
    }

    private String obtenerToken(ConfiguracionDian config) {
        TokenCache cache = tokensPorEmpresa.get(config.getEmpresaId());
        if (cache != null && cache.expiraEn().isAfter(Instant.now().plusSeconds(60)))
            return cache.accessToken();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", config.getFactusClientId());
        form.add("client_secret", config.getFactusClientSecret());
        form.add("username", config.getFactusUsername());
        form.add("password", config.getFactusPassword());

        JsonNode respuesta;
        try {
            respuesta = restClient.post()
                    .uri(baseUrl + "/oauth/token")
                    .header("Accept", "application/json")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("No se pudo autenticar con Factus — revisa las credenciales en Configuración: "
                    + mensajeError(e));
        }

        String accessToken = respuesta.path("access_token").asText(null);
        if (accessToken == null)
            throw new RuntimeException("Factus no devolvió un access_token válido");

        int expiresIn = respuesta.path("expires_in").asInt(3600);
        tokensPorEmpresa.put(config.getEmpresaId(), new TokenCache(accessToken, Instant.now().plusSeconds(expiresIn)));
        return accessToken;
    }

    // Factus normalmente responde {"status","message","data":{"message","errors":{...}}},
    // pero algunos errores (ej: 409 de factura pendiente) vienen planos sin "data" — se
    // cae a body.message antes que al mensaje genérico de Spring. "errors" puede ser un
    // objeto {campo:[mensajes]} o un arreglo de strings según el endpoint, así que no se
    // tipa: se muestra tal cual para poder depurar.
    private String mensajeError(RestClientResponseException e) {
        try {
            JsonNode body = e.getResponseBodyAs(JsonNode.class);
            JsonNode data = body != null ? body.path("data") : null;
            String mensaje = data != null && data.hasNonNull("message") ? data.get("message").asText()
                    : body != null && body.hasNonNull("message") ? body.get("message").asText()
                    : e.getMessage();
            JsonNode errores = data != null ? data.get("errors") : null;
            return errores != null ? mensaje + " — " + errores : mensaje;
        } catch (Exception parseFallo) {
            return e.getMessage() + " (" + e.getResponseBodyAsString() + ")";
        }
    }
}
