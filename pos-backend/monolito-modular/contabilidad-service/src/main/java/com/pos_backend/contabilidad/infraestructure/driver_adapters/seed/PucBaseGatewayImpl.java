package com.pos_backend.contabilidad.infraestructure.driver_adapters.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.contabilidad.domain.model.CuentaContable;
import com.pos_backend.contabilidad.domain.model.gateway.PucBaseGateway;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class PucBaseGatewayImpl implements PucBaseGateway {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<CuentaContable> cargarBase() {
        try (InputStream is = new ClassPathResource("seed/puc-base.json").getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<List<CuentaContable>>() {});
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar el PUC base: " + e.getMessage(), e);
        }
    }
}
