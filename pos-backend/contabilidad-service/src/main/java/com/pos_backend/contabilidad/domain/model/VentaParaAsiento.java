package com.pos_backend.contabilidad.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VentaParaAsiento {
    private Long ventaId;
    private String numeroVenta;
    private LocalDate fecha;
    private Long clienteId;
    private Double subtotal;
    private Double descuentoTotal;
    private Double totalIva;
    private Double total;
    private List<FormaPagoAsiento> formasPago;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class FormaPagoAsiento {
        private String metodo;   // EFECTIVO, TARJETA, TRANSFERENCIA, MIXTO
        private Double valor;
    }
}
