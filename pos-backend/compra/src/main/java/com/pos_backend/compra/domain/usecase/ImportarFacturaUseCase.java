package com.pos_backend.compra.domain.usecase;

import com.pos_backend.compra.domain.model.FacturaImportada;
import com.pos_backend.compra.domain.model.gateway.ImportadorFacturaGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImportarFacturaUseCase {

    private final ImportadorFacturaGateway importadorFacturaGateway;

    public FacturaImportada importarXml(byte[] contenido) {
        if (contenido == null || contenido.length == 0)
            throw new RuntimeException("El archivo XML está vacío");
        return importadorFacturaGateway.extraerDesdeXml(contenido);
    }

    public FacturaImportada importarPdf(byte[] contenido) {
        if (contenido == null || contenido.length == 0)
            throw new RuntimeException("El archivo PDF está vacío");
        return importadorFacturaGateway.extraerDesdePdf(contenido);
    }
}
