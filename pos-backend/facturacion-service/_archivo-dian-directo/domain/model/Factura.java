package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Factura {
    private Long facturaId;
    private String empresaId;
    private Long ventaId;

    private String numeroFactura;      // Prefijo + consecutivo, ej: SETP990000002
    private String cufe;
    private String qrUrl;
    private String ambiente;           // HABILITACION | PRODUCCION

    // GENERADA -> el XML se armó y firmó localmente
    // ENVIADA  -> se envió a la DIAN, esperando respuesta
    // ACEPTADA -> la DIAN la validó
    // RECHAZADA -> la DIAN la rechazó (ver respuestaDian)
    // ERROR    -> falló antes de llegar a la DIAN (certificado, datos, red)
    private String estado;

    private String respuestaDian;      // Mensaje/XML de respuesta o error, para depurar
    private String xmlFirmado;         // XML firmado completo (se guarda para reintentos y auditoría)

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
}
