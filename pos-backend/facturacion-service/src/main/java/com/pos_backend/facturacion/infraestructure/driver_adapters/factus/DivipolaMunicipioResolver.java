package com.pos_backend.facturacion.infraestructure.driver_adapters.factus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resuelve el código DIVIPOLA de municipio (obligatorio en customer.municipality_code
 * para Factus) a partir del texto libre de ciudad/departamento que tenemos en
 * cliente-service/empresa-service. Tabla completa (1122 municipios) tomada de
 * developers.factus.com.co/tablas-de-referencia/municipios/ y embebida en
 * resources/divipola-municipios.json — no hay endpoint de búsqueda en vivo en Factus,
 * solo esta tabla estática para descargar.
 */
@Component
public class DivipolaMunicipioResolver {

    // clave = "DEPARTAMENTO|CIUDAD" normalizados -> código
    private final Map<String, String> porDeptoYCiudad = new HashMap<>();
    // clave = ciudad normalizada -> código, solo si el nombre es único en todo el país
    private final Map<String, String> porCiudadUnica = new HashMap<>();

    public DivipolaMunicipioResolver() {
        try {
            JsonNode municipios = new ObjectMapper().readTree(
                    new ClassPathResource("divipola-municipios.json").getInputStream());
            Map<String, Integer> conteoCiudad = new HashMap<>();
            for (JsonNode m : municipios) {
                String code = m.get("code").asText();
                String ciudad = normalizar(m.get("name").asText());
                String depto = normalizar(m.get("department").get("name").asText());
                porDeptoYCiudad.put(depto + "|" + ciudad, code);
                conteoCiudad.merge(ciudad, 1, Integer::sum);
                porCiudadUnica.put(ciudad, code);
            }
            // Si el mismo nombre de ciudad aparece en más de un departamento (ej: "La Union"),
            // el atajo por-solo-ciudad es ambiguo — se saca para no adivinar mal.
            conteoCiudad.forEach((ciudad, veces) -> { if (veces > 1) porCiudadUnica.remove(ciudad); });
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el catálogo DIVIPOLA de municipios", e);
        }
    }

    /** Devuelve el código DIVIPOLA o null si no se pudo determinar con certeza. */
    public String resolver(String departamento, String ciudad) {
        if (ciudad == null || ciudad.isBlank()) return null;
        String c = normalizar(ciudad);
        String d = normalizar(departamento != null ? departamento : "");

        String exacto = porDeptoYCiudad.get(d + "|" + c);
        if (exacto != null) return exacto;

        String porCiudad = porCiudadUnica.get(c);
        if (porCiudad != null) return porCiudad;

        // ponytail: heurística ingenua para casos como "Bogotá" sin el sufijo "D.C." que
        // trae la tabla ("BOGOTA DC"). Se limita a nombres de 4+ letras para no hacer
        // matches raros con ciudades cortas; si da problemas, cambiar por distancia de
        // edición (Levenshtein) o normalizar alias conocidos a mano.
        if (c.length() >= 4) {
            for (Map.Entry<String, String> e : porCiudadUnica.entrySet()) {
                if (e.getKey().startsWith(c) || c.startsWith(e.getKey())) return e.getValue();
            }
        }
        return null;
    }

    private static String normalizar(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ").trim();
    }
}
