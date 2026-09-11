package com.pos_backend.facturacion.domain.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FacturaDetalle {
    private Factura factura;
    private VentaRemota venta;
    private ClienteRemoto cliente;
    private EmpresaRemota empresa;
    private String qrCodeBase64;   // PNG en base64, generado en el servidor a partir de factura.qrUrl
}
