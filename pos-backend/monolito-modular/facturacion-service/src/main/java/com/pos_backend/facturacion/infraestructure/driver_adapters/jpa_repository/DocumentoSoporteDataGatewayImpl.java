package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.facturacion.domain.model.DocumentoSoporte;
import com.pos_backend.facturacion.domain.model.gateway.DocumentoSoporteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository @RequiredArgsConstructor
public class DocumentoSoporteDataGatewayImpl implements DocumentoSoporteGateway {

    private final DocumentoSoporteDataJpaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override public DocumentoSoporte guardar(DocumentoSoporte n) {
        DocumentoSoporteData d = new DocumentoSoporteData();
        BeanUtils.copyProperties(n, d, "items");
        try {
            d.setItemsJson(objectMapper.writeValueAsString(n.getItems()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar los ítems del documento soporte");
        }
        return toDomain(repository.save(d));
    }

    @Override public DocumentoSoporte buscarPorId(Long id, String empresaId) {
        return repository.findByDocumentoSoporteIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override public DocumentoSoporte buscarPorCompraId(Long compraId, String empresaId) {
        return repository.findByCompraIdAndEmpresaId(compraId, empresaId).map(this::toDomain).orElse(null);
    }

    @Override public List<DocumentoSoporte> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByDocumentoSoporteIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override public void eliminar(Long documentoSoporteId, String empresaId) {
        repository.deleteByDocumentoSoporteIdAndEmpresaId(documentoSoporteId, empresaId);
    }

    private DocumentoSoporte toDomain(DocumentoSoporteData d) {
        DocumentoSoporte n = new DocumentoSoporte();
        BeanUtils.copyProperties(d, n, "itemsJson");
        try {
            if (d.getItemsJson() != null)
                n.setItems(objectMapper.readValue(d.getItemsJson(),
                        new TypeReference<List<DocumentoSoporte.ItemDocumentoSoporte>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Error al leer los ítems del documento soporte");
        }
        return n;
    }
}
