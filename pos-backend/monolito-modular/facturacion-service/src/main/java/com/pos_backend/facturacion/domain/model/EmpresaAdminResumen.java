package com.pos_backend.facturacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Fila de la tabla del panel de super administrador (ver AdminUseCase). Trae
// todos los datos de configuración de la empresa (no solo lo mínimo para
// identificarla) para que Plutus365 pueda ver de un vistazo con quién está
// tratando al hacer el onboarding de Factus — las credenciales de Factus en
// sí se piden aparte con GET /empresas/{id}/factus.
@Getter
@AllArgsConstructor
public class EmpresaAdminResumen {
    private String empresaId;
    private String nombre;
    private String tipoPersona;
    private String tipoDocumento;
    private String numeroDocumento;
    private String dv;
    private String regimenFiscal;
    private String correo;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String departamento;
    private String pais;
    private boolean factusConfigurado;
    private Long planId;
    private int facturasEmitidas;
    private int facturasAceptadas;
    private int facturasRechazadas;
    private int facturasError;
    private Integer foliosAsignados;   // null = sin cupo cargado todavía
    private Integer foliosRestantes;   // null = sin cupo cargado; puede ser negativo si se pasó
}
