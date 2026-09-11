package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pos_backend.venta.domain.model.FacturaRecurrente;
import com.pos_backend.venta.domain.model.VentaItem;
import com.pos_backend.venta.domain.model.gateway.FacturaRecurrenteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository @RequiredArgsConstructor
public class FacturaRecurrenteDataGatewayImpl implements FacturaRecurrenteGateway {

    private final FacturaRecurrenteDataJpaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override public FacturaRecurrente guardar(FacturaRecurrente r) {
        FacturaRecurrenteData d = new FacturaRecurrenteData();
        BeanUtils.copyProperties(r, d, "items");
        try {
            d.setItemsJson(objectMapper.writeValueAsString(r.getItems()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar los ítems de la recurrencia");
        }
        return toDomain(repository.save(d));
    }

    @Override public FacturaRecurrente buscarPorId(Long id, String empresaId) {
        return repository.findByRecurrenteIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override public List<FacturaRecurrente> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByProximaGeneracionAsc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override public void eliminar(Long id, String empresaId) {
        repository.findByRecurrenteIdAndEmpresaId(id, empresaId).ifPresent(repository::delete);
    }

    private FacturaRecurrente toDomain(FacturaRecurrenteData d) {
        FacturaRecurrente r = new FacturaRecurrente();
        BeanUtils.copyProperties(d, r, "itemsJson");
        try {
            if (d.getItemsJson() != null)
                r.setItems(objectMapper.readValue(d.getItemsJson(), new TypeReference<List<VentaItem>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Error al leer los ítems de la recurrencia");
        }
        return r;
    }
}
