package com.pos_backend.compra.domain.model.gateway;

import com.pos_backend.compra.domain.model.FacturaImportada;

public interface ImportadorFacturaGateway {
    FacturaImportada extraerDesdeXml(byte[] contenido);
    FacturaImportada extraerDesdePdf(byte[] contenido);
}
