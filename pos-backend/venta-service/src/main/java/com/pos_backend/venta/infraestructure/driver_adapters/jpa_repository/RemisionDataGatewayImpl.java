package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pos_backend.venta.domain.model.Remision;
import com.pos_backend.venta.domain.model.VentaItem;
import com.pos_backend.venta.domain.model.gateway.RemisionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository @RequiredArgsConstructor
public class RemisionDataGatewayImpl implements RemisionGateway {

    private final RemisionDataJpaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public Remision guardar(Remision r) {
        RemisionData d = new RemisionData();
        BeanUtils.copyProperties(r, d, "items");
        try {
            d.setItemsJson(objectMapper.writeValueAsString(r.getItems()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar los ítems de la remisión");
        }
        return toDomain(repository.save(d));
    }

    @Override
    public Remision buscarPorId(Long id, String empresaId) {
        return repository.findByRemisionIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override
    public List<Remision> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByRemisionIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Remision> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta) {
        return repository.findByEmpresaIdAndFechaBetween(empresaId, desde.atStartOfDay(), hasta.atTime(23, 59, 59))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public String generarSiguienteNumero(String empresaId) {
        return "REM-" + String.format("%05d", repository.countByEmpresaId(empresaId) + 1);
    }

    private Remision toDomain(RemisionData d) {
        Remision r = new Remision();
        BeanUtils.copyProperties(d, r, "itemsJson");
        try {
            if (d.getItemsJson() != null)
                r.setItems(objectMapper.readValue(d.getItemsJson(), new TypeReference<List<VentaItem>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Error al leer los ítems de la remisión");
        }
        return r;
    }
}
