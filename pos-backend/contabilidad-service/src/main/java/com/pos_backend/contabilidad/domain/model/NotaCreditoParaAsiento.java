package com.pos_backend.contabilidad.domain.model;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NotaCreditoParaAsiento {
    private Long notaCreditoId;
    private String numeroNota;
    private LocalDate fecha;
    private Double subtotal;
    private Double totalIva;
    private Double total;
    /** Si la venta original se pagó en efectivo, el reintegro sale de caja; si no, de bancos. */
    private String metodoPagoOriginal;
}
