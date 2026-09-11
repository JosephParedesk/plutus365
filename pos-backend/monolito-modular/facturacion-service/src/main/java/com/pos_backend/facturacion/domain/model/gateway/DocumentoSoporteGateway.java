package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.DocumentoSoporte;
import java.util.List;

public interface DocumentoSoporteGateway {
    DocumentoSoporte guardar(DocumentoSoporte documento);
    DocumentoSoporte buscarPorId(Long documentoSoporteId, String empresaId);
    DocumentoSoporte buscarPorCompraId(Long compraId, String empresaId);
    List<DocumentoSoporte> listar(String empresaId);
    void eliminar(Long documentoSoporteId, String empresaId);
}
