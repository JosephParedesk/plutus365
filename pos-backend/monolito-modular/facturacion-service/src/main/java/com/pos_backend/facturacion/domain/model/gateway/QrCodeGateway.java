package com.pos_backend.facturacion.domain.model.gateway;

public interface QrCodeGateway {
    /** Genera un QR (PNG) a partir de un texto/URL, devuelto en base64. */
    String generarBase64(String contenido);
}
