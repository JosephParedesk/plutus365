package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.facturacion.domain.model.RecepcionDocumento;
import com.pos_backend.facturacion.domain.model.gateway.RecepcionDocumentoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

// A diferencia de las notas (que siempre insertan una fila nueva por cada
// intento), acá SÍ hace falta actualizar en el sitio: "cargar" crea la fila y
// "emitirEvento" le va agregando eventos a esa misma fila con el tiempo.
@Repository @RequiredArgsConstructor
public class RecepcionDocumentoDataGatewayImpl implements RecepcionDocumentoGateway {

    private final RecepcionDocumentoDataJpaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override public RecepcionDocumento guardar(RecepcionDocumento r) {
        RecepcionDocumentoData d = repository.findByCompraIdAndEmpresaId(r.getCompraId(), r.getEmpresaId())
                .orElse(new RecepcionDocumentoData());
        Long id = d.getRecepcionId();
        BeanUtils.copyProperties(r, d, "eventos");
        d.setRecepcionId(id);
        try {
            d.setEventosJson(objectMapper.writeValueAsString(r.getEventos()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar los eventos de la recepción");
        }
        return toDomain(repository.save(d));
    }

    @Override public RecepcionDocumento buscarPorId(Long id, String empresaId) {
        return repository.findByRecepcionIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override public RecepcionDocumento buscarPorCompraId(Long compraId, String empresaId) {
        return repository.findByCompraIdAndEmpresaId(compraId, empresaId).map(this::toDomain).orElse(null);
    }

    @Override public List<RecepcionDocumento> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByRecepcionIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    private RecepcionDocumento toDomain(RecepcionDocumentoData d) {
        RecepcionDocumento r = new RecepcionDocumento();
        BeanUtils.copyProperties(d, r, "eventosJson");
        try {
            r.setEventos(d.getEventosJson() != null
                    ? objectMapper.readValue(d.getEventosJson(), new TypeReference<List<RecepcionDocumento.EventoEmitido>>() {})
                    : new java.util.ArrayList<>());
        } catch (Exception e) {
            throw new RuntimeException("Error al leer los eventos de la recepción");
        }
        return r;
    }
}
