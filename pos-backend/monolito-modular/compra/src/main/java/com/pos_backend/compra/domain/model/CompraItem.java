package com.pos_backend.compra.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CompraItem {
    private Long itemId;
    private String tipo;          // ACTIVO_FIJO, PRODUCTO, GASTO_CUENTA
    private String productoSku;
    private String cuentaContableCodigo; // solo aplica cuando tipo = GASTO_CUENTA
    private String descripcion;
    private Double cantidad;
    private Double valorUnitario;
    private Double descuento;
    private String impuestoCargo;     // ej: "IVA 19%"
    private String impuestoRetencion; // ej: "Retefuente 3.5%"
    private Double valorTotal;
}