package com.pos_backend.venta.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CajaSesion {
    private Long cajaId;
    private String empresaId;

    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    private Double montoApertura;          // base declarada al abrir
    private Double montoCierreDeclarado;    // lo que el cajero contó físicamente
    private Double montoCierreCalculado;    // lo que el sistema calcula que debería haber
    private Double diferencia;              // declarado - calculado (positivo = sobrante, negativo = faltante)

    private Double totalVentasEfectivo;
    private Double totalVentasOtros;        // tarjeta, transferencia, etc.
    private Integer numeroVentas;

    private String estado;                  // ABIERTA, CERRADA
    private String usuarioApertura;
    private String usuarioCierre;
    private String observaciones;
}
