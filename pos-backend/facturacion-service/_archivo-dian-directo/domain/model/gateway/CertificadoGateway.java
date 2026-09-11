package com.pos_backend.facturacion.domain.model.gateway;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public interface CertificadoGateway {

    /** Guarda el .p12 como firmadigital_{empresaId}.p12 y el password cifrado. */
    void guardarCertificado(String empresaId, byte[] contenidoPfx, String password);

    boolean existeCertificado(String empresaId);

    /** Carga la clave privada y la cadena de certificados (firmante, emisor, raíz) para firmar un XML. */
    MaterialFirma cargarMaterialFirma(String empresaId);

    record MaterialFirma(
            PrivateKey privateKey,
            X509Certificate firmante,
            X509Certificate emisor,
            X509Certificate caRaiz
    ) {}
}
