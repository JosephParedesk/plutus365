package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Recepción de una factura electrónica DE UN PROVEEDOR (eventos RADIAN,
 * Resolución 000085 de 2022 — factura electrónica como título valor,
 * verificado 2026-09-05 contra el PDF oficial de la DIAN y la documentación
 * de Siigo/Alegra) — a diferencia de todo lo demás en este paquete, acá
 * Plutus365 no emite el documento, lo RECIBE y debe confirmarle a la DIAN qué
 * pasó con él (acuse de recibo, recibo del bien/servicio, reclamo o
 * aceptación expresa). Se ancla a la `compra` que ya trae el CUFE del
 * proveedor desde la importación de XML (`compra.cufeProveedor`, ver
 * `compra-service`) — sin eso no hay nada que cargar en Factus.
 *
 * **Solo aplica a compras A CRÉDITO** — la Resolución 000085/2022 es explícita:
 * estos eventos rigen para operaciones a crédito, no de contado (se valida en
 * `RecepcionDocumentoUseCase.cargar`).
 *
 * Flujo: 1) `cargar` sube el CUFE a Factus (`/v2/receptions/upload`) y resuelve
 * el `billId` interno de Factus para ese documento; 2) `emitirEvento` dispara
 * cada evento RADIAN uno por uno sobre ese `billId`. La "aceptación tácita"
 * (034) NO se puede emitir manualmente — la genera Factus/DIAN sola si pasan
 * 3 días hábiles sin reclamo ni aceptación expresa tras el evento de recibo.
 */
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class RecepcionDocumento {
    private Long recepcionId;
    private String empresaId;

    private Long compraId;         // compra que trae el CUFE del proveedor
    private String cufe;
    private String billId;         // id interno de Factus para este documento recibido (null hasta "cargar")

    private String estado;         // PENDIENTE_CARGA, CARGADO, ERROR
    private String respuestaDian;

    private List<EventoEmitido> eventos = new ArrayList<>();

    private LocalDateTime fechaCarga;
    private String creadoPor;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class EventoEmitido {
        private String codigo;         // 030..034
        private String nombre;
        private LocalDateTime fecha;
        private String personaNombre;
        private String respuestaDian;
    }

    /** Datos de quien firma el evento — se piden en cada llamada, no se guardan como "usuario responsable" fijo. */
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class Persona {
        private String tipoDocumento;      // CC, NIT, CE, TI, PASAPORTE, RC (misma tabla que clientes/proveedores)
        private String numeroDocumento;
        private String dv;                 // solo si tipoDocumento = NIT
        private String nombres;
        private String apellidos;
        private String cargo;
        private String area;               // "organization_department" en Factus
    }
}
