package com.pos_backend.venta.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Plantilla de facturación recurrente: arriendos, suscripciones, mantenimientos,
 * cualquier cobro que se repite con la misma periodicidad y valor.
 *
 * DECISIÓN IMPORTANTE: el sistema NO genera la venta solo, sin intervención.
 * Cuando llega la fecha, la recurrencia queda marcada como PENDIENTE y el usuario
 * la confirma con un clic. Generar ventas automáticas de madrugada —que descuentan
 * inventario y crean asientos contables— sin que nadie las revise es una forma
 * fácil de acumular errores silenciosos que después nadie sabe de dónde salieron.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FacturaRecurrente {
    private Long recurrenteId;
    private String empresaId;
    private String nombre;              // "Arriendo local 2 - Cliente X"

    private Long clienteId;
    private String clienteNombre;
    private String clienteCorreo;

    private List<VentaItem> items;
    private Double descuentoTotal;
    private Double totalIva;
    private Double total;

    private String periodicidad;        // MENSUAL, BIMESTRAL, TRIMESTRAL, SEMESTRAL, ANUAL
    private Integer diaGeneracion;      // día del mes en que toca cobrar (1-28)
    private LocalDate fechaInicio;
    private LocalDate fechaFin;         // null = indefinida
    private LocalDate proximaGeneracion;
    private LocalDate ultimaGeneracion;

    private Integer vecesGeneradas;
    private Boolean activa;

    private Long centroCostoId;
    private String centroCostoNombre;
    private String metodoPagoPredeterminado;
    private String observaciones;
    private String creadoPor;
}
