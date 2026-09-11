package com.pos_backend.inventario.domain.model;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Registro histórico de cada cambio de stock — la base del Kárdex.
 * Se guarda el saldo ANTES y DESPUÉS para que el Kárdex sea auditable
 * aunque después cambien los costos del producto.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class MovimientoInventario {
    private Long movimientoId;
    private String empresaId;

    private String sku;
    private String nombreProducto;

    private LocalDateTime fecha;
    private String tipo;            // ENTRADA_COMPRA, SALIDA_VENTA, AJUSTE_ENTRADA, AJUSTE_SALIDA, CREACION
    private String documentoOrigen; // número de compra/venta que lo causó
    private String descripcion;

    private Integer cantidad;       // siempre positiva; el signo lo da el tipo
    private Integer saldoAnterior;
    private Integer saldoNuevo;

    private Double costoUnitario;
    private Double valorMovimiento;

    private String usuario;
}
