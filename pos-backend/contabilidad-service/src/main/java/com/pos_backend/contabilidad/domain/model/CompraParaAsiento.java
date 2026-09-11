package com.pos_backend.contabilidad.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CompraParaAsiento {
    private Long compraId;
    private String tipoTransaccion;  // FACTURA_COMPRA, DOCUMENTO_SOPORTE, FACTURA_COMPRA_ELECTRONICA, RECIBO_PAGO, NOTA_DEBITO, AJUSTE_CARTERA
    private String numeroComprobante;
    private LocalDate fecha;
    private Long proveedorId;
    private Double totalPagar;
    private Double totalIva;
    private Boolean tieneCreditoProveedor;
    private Long compraReferenciaId;   // para RECIBO_PAGO / NOTA_DEBITO / AJUSTE_CARTERA
    private List<ItemAsiento> items;
    private List<FormaPagoAsiento> formasPago;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ItemAsiento {
        private String tipo;    // ACTIVO_FIJO, PRODUCTO, GASTO_CUENTA
        private String descripcion;
        private Double valorTotal;
        private String cuentaContableCodigo; // si el usuario ya eligió la cuenta explícitamente (típico en GASTO_CUENTA)
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class FormaPagoAsiento {
        private String metodo;  // EFECTIVO, TRANSFERENCIA, TARJETA, CREDITO_PROVEEDOR
        private Double valor;
    }
}
