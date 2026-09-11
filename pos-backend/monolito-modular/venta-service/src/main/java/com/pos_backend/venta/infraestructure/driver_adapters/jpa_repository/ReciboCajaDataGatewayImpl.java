package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pos_backend.venta.domain.model.ReciboCaja;
import com.pos_backend.venta.domain.model.gateway.ReciboCajaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository @RequiredArgsConstructor
public class ReciboCajaDataGatewayImpl implements ReciboCajaGateway {

    private final ReciboCajaDataJpaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override public ReciboCaja guardar(ReciboCaja r) {
        ReciboCajaData d = new ReciboCajaData();
        BeanUtils.copyProperties(r, d, "aplicaciones");
        try {
            d.setAplicacionesJson(objectMapper.writeValueAsString(r.getAplicaciones()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar las aplicaciones del recibo");
        }
        return toDomain(repository.save(d));
    }

    @Override public ReciboCaja buscarPorId(Long id, String empresaId) {
        return repository.findByReciboIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override public List<ReciboCaja> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByReciboIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override public List<ReciboCaja> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta) {
        return repository.findByEmpresaIdAndFechaBetween(empresaId, desde.atStartOfDay(), hasta.atTime(23, 59, 59))
                .stream().map(this::toDomain).toList();
    }

    @Override public String generarSiguienteNumero(String empresaId) {
        return "RC-" + String.format("%05d", repository.countByEmpresaId(empresaId) + 1);
    }

    private ReciboCaja toDomain(ReciboCajaData d) {
        ReciboCaja r = new ReciboCaja();
        BeanUtils.copyProperties(d, r, "aplicacionesJson");
        try {
            if (d.getAplicacionesJson() != null)
                r.setAplicaciones(objectMapper.readValue(d.getAplicacionesJson(),
                        new TypeReference<List<ReciboCaja.AplicacionCobro>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Error al leer las aplicaciones del recibo");
        }
        return r;
    }
}
