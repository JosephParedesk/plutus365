package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.*;

public interface XmlFacturaGateway {

    ResultadoXml construirYFirmar(
            EmpresaRemota empresa,
            ClienteRemoto cliente,
            VentaRemota venta,
            ConfiguracionDian config,
            String numeroFactura,
            CertificadoGateway.MaterialFirma material
    );

    record ResultadoXml(String xmlFirmado, String cufe, String qrUrl) {}
}
