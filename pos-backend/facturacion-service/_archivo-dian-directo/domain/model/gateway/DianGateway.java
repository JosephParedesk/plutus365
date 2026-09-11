package com.pos_backend.facturacion.domain.model.gateway;

public interface DianGateway {

    /**
     * Envía la factura firmada (comprimida en zip y codificada en base64) a la DIAN.
     * @param zipBase64 el .zip del XML firmado, codificado en base64
     * @param ambiente HABILITACION | PRODUCCION
     * @param testSetId requerido solo en HABILITACION (identifica el set de pruebas asignado por la DIAN)
     */
    RespuestaDian enviarFactura(
            String zipBase64,
            String nombreArchivoZip,
            CertificadoGateway.MaterialFirma material,
            String ambiente,
            String testSetId
    );

    record RespuestaDian(boolean exitosa, String mensaje, String xmlRespuesta) {}
}
