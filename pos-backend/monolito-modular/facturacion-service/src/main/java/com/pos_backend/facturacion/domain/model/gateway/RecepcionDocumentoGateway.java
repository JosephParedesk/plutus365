package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.RecepcionDocumento;
import java.util.List;

public interface RecepcionDocumentoGateway {
    RecepcionDocumento guardar(RecepcionDocumento recepcion);
    RecepcionDocumento buscarPorId(Long id, String empresaId);
    RecepcionDocumento buscarPorCompraId(Long compraId, String empresaId);
    List<RecepcionDocumento> listar(String empresaId);
}
