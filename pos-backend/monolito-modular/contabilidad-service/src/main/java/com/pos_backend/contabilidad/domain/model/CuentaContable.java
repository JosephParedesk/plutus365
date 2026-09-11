package com.pos_backend.contabilidad.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CuentaContable {
    private Long id;
    private String empresaId;
    private String codigo;          // código jerárquico PUC, ej: 11050501
    private String nombre;
    private String nivel;           // CLASE, GRUPO, CUENTA, SUBCUENTA, AUXILIAR
    private String codigoPadre;     // null solo para las CLASE (1 dígito)
    private String naturaleza;      // DEBITO, CREDITO
    private Boolean esTransaccional; // si puede recibir movimientos directos (normalmente solo AUXILIAR)
    private String categoria;       // ej: "Caja - Bancos", "Clientes", "Proveedores"
    private String detalleSaldos;   // SIN_DETALLE, DETALLE_VENCIMIENTOS, DETALLE_TERCEROS
    private Boolean activa;
    private Boolean personalizada;  // true si la creó el usuario (no viene del PUC base precargado)
}
