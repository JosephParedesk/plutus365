package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Factura {
    private Long facturaId;
    private String empresaId;
    private Long ventaId;

    private String referenceCode;      // el que se le mandó a Factus — hace falta para poder eliminarla si quedó pendiente
    private String numeroFactura;      // Asignado por Factus, ej: SETP990000002
    private String cufe;
    private String qrUrl;
    private String urlDocumento;       // Vista pública del documento en Factus
    private String ambiente;           // PRUEBAS | PRODUCCION

    // ACEPTADA  -> Factus la validó y transmitió
    // RECHAZADA -> Factus/la DIAN la rechazó (ver respuestaDian)
    // ERROR     -> falló antes de completarse (config, datos, red)
    private String estado;

    private String respuestaDian;      // Mensaje de respuesta o error, para depurar

    private LocalDateTime fechaEmision;
    private LocalDateTime fechaEnvio;
}
