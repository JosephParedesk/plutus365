package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.facturacion.domain.model.NotaDebito;
import com.pos_backend.facturacion.domain.model.gateway.NotaDebitoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository @RequiredArgsConstructor
public class NotaDebitoDataGatewayImpl implements NotaDebitoGateway {

    private final NotaDebitoDataJpaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override public NotaDebito guardar(NotaDebito n) {
        NotaDebitoData d = new NotaDebitoData();
        BeanUtils.copyProperties(n, d, "items");
        try {
            d.setItemsJson(objectMapper.writeValueAsString(n.getItems()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar los ítems de la nota débito");
        }
        return toDomain(repository.save(d));
    }

    @Override public NotaDebito buscarPorId(Long id, String empresaId) {
        return repository.findByNotaDebitoIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override public List<NotaDebito> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByNotaDebitoIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override public List<NotaDebito> listarPorFactura(Long facturaId, String empresaId) {
        return repository.findByFacturaIdAndEmpresaId(facturaId, empresaId).stream().map(this::toDomain).toList();
    }

    @Override public void eliminar(Long notaDebitoId, String empresaId) {
        repository.deleteByNotaDebitoIdAndEmpresaId(notaDebitoId, empresaId);
    }

    private NotaDebito toDomain(NotaDebitoData d) {
        NotaDebito n = new NotaDebito();
        BeanUtils.copyProperties(d, n, "itemsJson");
        try {
            if (d.getItemsJson() != null)
                n.setItems(objectMapper.readValue(d.getItemsJson(),
                        new TypeReference<List<NotaDebito.ItemNotaDebito>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Error al leer los ítems de la nota débito");
        }
        return n;
    }
}
