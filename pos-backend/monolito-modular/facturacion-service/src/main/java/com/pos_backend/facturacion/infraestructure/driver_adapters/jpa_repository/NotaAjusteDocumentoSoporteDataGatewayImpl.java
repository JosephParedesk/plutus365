package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.facturacion.domain.model.DocumentoSoporte;
import com.pos_backend.facturacion.domain.model.NotaAjusteDocumentoSoporte;
import com.pos_backend.facturacion.domain.model.gateway.NotaAjusteDocumentoSoporteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository @RequiredArgsConstructor
public class NotaAjusteDocumentoSoporteDataGatewayImpl implements NotaAjusteDocumentoSoporteGateway {

    private final NotaAjusteDocumentoSoporteDataJpaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override public NotaAjusteDocumentoSoporte guardar(NotaAjusteDocumentoSoporte n) {
        NotaAjusteDocumentoSoporteData d = new NotaAjusteDocumentoSoporteData();
        BeanUtils.copyProperties(n, d, "items");
        try {
            d.setItemsJson(objectMapper.writeValueAsString(n.getItems()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar los ítems de la nota de ajuste");
        }
        return toDomain(repository.save(d));
    }

    @Override public NotaAjusteDocumentoSoporte buscarPorId(Long id, String empresaId) {
        return repository.findByNotaAjusteIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override public List<NotaAjusteDocumentoSoporte> listarPorDocumentoSoporte(Long documentoSoporteId, String empresaId) {
        return repository.findByDocumentoSoporteIdAndEmpresaIdOrderByNotaAjusteIdDesc(documentoSoporteId, empresaId)
                .stream().map(this::toDomain).toList();
    }

    @Override public List<NotaAjusteDocumentoSoporte> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByNotaAjusteIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override public void eliminar(Long id, String empresaId) {
        repository.deleteByNotaAjusteIdAndEmpresaId(id, empresaId);
    }

    private NotaAjusteDocumentoSoporte toDomain(NotaAjusteDocumentoSoporteData d) {
        NotaAjusteDocumentoSoporte n = new NotaAjusteDocumentoSoporte();
        BeanUtils.copyProperties(d, n, "itemsJson");
        try {
            if (d.getItemsJson() != null)
                n.setItems(objectMapper.readValue(d.getItemsJson(),
                        new TypeReference<List<DocumentoSoporte.ItemDocumentoSoporte>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Error al leer los ítems de la nota de ajuste");
        }
        return n;
    }
}
