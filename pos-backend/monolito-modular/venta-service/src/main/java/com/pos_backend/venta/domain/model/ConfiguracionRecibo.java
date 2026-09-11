package com.pos_backend.venta.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ConfiguracionRecibo {
    private String empresaId;          // PK — un registro por empresa

    private String anchoPapel;         // 58MM | 80MM (impresoras térmicas típicas)
    private String tamanioFuente;      // PEQUENA | NORMAL | GRANDE
    private String mensajePie;         // reemplaza "¡Gracias por tu compra!"

    private Boolean mostrarLogo;
    private Boolean mostrarDireccionEmpresa;
    private Boolean mostrarAtendidoPor;
}
