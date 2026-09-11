package com.pos_backend.nomina.infraestructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.nomina.domain.model.Nomina;
import com.pos_backend.nomina.domain.model.NominaDetalle;
import com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository.NominaData;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NominaMapper {

    // Filas de antes del cambio a horasExtra como lista (ver NominaDetalle) tienen
    // campos viejos (horasExtraDiurnas, recargoNocturno, etc.) que ya no existen en
    // la clase — sin esto, Jackson tira "Error al leer el detalle de la nómina" al
    // encontrar una propiedad desconocida y ni el propio nomina-service puede leer
    // su nómina histórica.
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public NominaData toData(Nomina n) {
        NominaData d = new NominaData();
        BeanUtils.copyProperties(n, d, "detalles");
        try {
            d.setDetallesJson(objectMapper.writeValueAsString(n.getDetalles()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar el detalle de la nómina");
        }
        return d;
    }

    public Nomina toDomain(NominaData d) {
        if (d == null) return null;
        Nomina n = new Nomina();
        BeanUtils.copyProperties(d, n, "detallesJson");
        try {
            if (d.getDetallesJson() != null)
                n.setDetalles(objectMapper.readValue(d.getDetallesJson(), new TypeReference<List<NominaDetalle>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Error al leer el detalle de la nómina");
        }
        return n;
    }
}
