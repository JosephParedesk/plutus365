package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.NotaAjusteDocumentoSoporte;
import java.util.List;

public interface NotaAjusteDocumentoSoporteGateway {
    NotaAjusteDocumentoSoporte guardar(NotaAjusteDocumentoSoporte nota);
    NotaAjusteDocumentoSoporte buscarPorId(Long id, String empresaId);
    List<NotaAjusteDocumentoSoporte> listarPorDocumentoSoporte(Long documentoSoporteId, String empresaId);
    List<NotaAjusteDocumentoSoporte> listar(String empresaId);
    void eliminar(Long id, String empresaId);
}
