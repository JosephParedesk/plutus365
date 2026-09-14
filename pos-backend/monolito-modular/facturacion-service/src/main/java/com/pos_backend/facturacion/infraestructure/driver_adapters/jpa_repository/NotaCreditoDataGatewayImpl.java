package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.facturacion.domain.model.NotaCredito;
import com.pos_backend.facturacion.domain.model.gateway.NotaCreditoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository @RequiredArgsConstructor
public class NotaCreditoDataGatewayImpl implements NotaCreditoGateway {

    private final NotaCreditoDataJpaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override public NotaCredito guardar(NotaCredito n) {
        NotaCreditoData d = new NotaCreditoData();
        BeanUtils.copyProperties(n, d, "items");
        try {
            d.setItemsJson(objectMapper.writeValueAsString(n.getItems()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar los ítems de la nota crédito");
        }
        return toDomain(repository.save(d));
    }

    @Override public NotaCredito buscarPorId(Long id, String empresaId) {
        return repository.findByNotaCreditoIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override public List<NotaCredito> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByNotaCreditoIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override public List<NotaCredito> listarPorFactura(Long facturaId, String empresaId) {
        return repository.findByFacturaIdAndEmpresaId(facturaId, empresaId).stream().map(this::toDomain).toList();
    }

    @Override public long contarPorEmpresa(String empresaId) {
        return repository.countByEmpresaId(empresaId);
    }

    // Un deleteByX derivado necesita transacción propia — ver el mismo comentario
    // en NominaElectronicaDataGatewayImpl (bug real encontrado ahí).
    @Override @Transactional public void eliminar(Long notaCreditoId, String empresaId) {
        repository.deleteByNotaCreditoIdAndEmpresaId(notaCreditoId, empresaId);
    }

    private NotaCredito toDomain(NotaCreditoData d) {
        NotaCredito n = new NotaCredito();
        BeanUtils.copyProperties(d, n, "itemsJson");
        try {
            if (d.getItemsJson() != null)
                n.setItems(objectMapper.readValue(d.getItemsJson(),
                        new TypeReference<List<NotaCredito.ItemNotaCredito>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Error al leer los ítems de la nota crédito");
        }
        return n;
    }
}
