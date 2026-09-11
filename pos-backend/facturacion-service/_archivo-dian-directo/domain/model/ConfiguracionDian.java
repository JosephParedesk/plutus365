package com.pos_backend.facturacion.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ConfiguracionDian {

    // Debajo de cuántos números disponibles se avisa que el rango se está agotando.
    public static final long UMBRAL_ALERTA_RANGO = 50;

    private String empresaId;          // PK - un registro por empresa

    // Resolución de numeración autorizada por la DIAN
    private String resolucionNumero;
    private String resolucionFechaInicio;   // yyyy-MM-dd
    private String resolucionFechaFin;      // yyyy-MM-dd
    private String prefijo;                 // ej: SETP
    private Long rangoDesde;
    private Long rangoHasta;
    private Long consecutivoActual;         // próximo número a usar

    // Datos técnicos entregados por la DIAN al habilitarse como facturador
    private String softwareId;
    private String softwarePin;
    private String claveTecnica;

    private String ambiente;                // HABILITACION | PRODUCCION
    private String testSetId;               // Asignado por la DIAN al habilitarte (solo ambiente HABILITACION)

    // Metadatos del certificado — el archivo y el password viven en disco/cifrados,
    // acá solo guardamos lo necesario para mostrar estado en el frontend.
    private String certificadoNombreArchivo;   // firmadigital_{empresaId}.p12
    private String certificadoCargadoEn;       // fecha ISO de la última carga
    private Boolean certificadoActivo;

    // Calculados al vuelo en ConfiguracionDianUseCase — no se persisten en BD.
    private Long numerosRestantes;
    private Boolean alertaRangoBajo;
}
