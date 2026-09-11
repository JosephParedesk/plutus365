package com.pos_backend.empresa.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Empresa {
    private String empresaId;         // PK - mismo empresaId del JWT ("EMP-" + cedula)

    // Clasificación DIAN del emisor
    private String tipoPersona;       // NATURAL | JURIDICA
    private String tipoDocumento;     // NIT, CC
    private String numeroDocumento;
    private String dv;                // Dígito de verificación (solo NIT)
    private String regimenFiscal;     // RESPONSABLE_IVA | NO_RESPONSABLE_IVA
    // Solo aplica si regimenFiscal=RESPONSABLE_IVA — DIAN asigna la periodicidad según
    // ingresos del año anterior, el sistema no la calcula, la captura el usuario.
    private String periodicidadIva;   // BIMESTRAL | CUATRIMESTRAL | null
    // Si además es agente retenedor de retención en la fuente (no todo responsable de
    // IVA lo es). Usado solo para el widget de "Impuestos próximos" del dashboard.
    private Boolean agenteRetenedor;

    // Identificación / nombre
    private String razonSocial;       // Persona jurídica
    private String nombres;           // Persona natural
    private String apellidos;         // Persona natural
    private String nombreComercial;   // Opcional, nombre de fantasía

    // Contacto
    private String correo;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String departamento;
    private String pais;

    private String logoUrl;
    private String colorPrincipal;    // Hex (#RRGGBB) — usado en el encabezado de los correos que se le mandan a los clientes
    private String moneda;            // Default: COP

    // Apagado por defecto a propósito: Facturación (la parte fuerte del software)
    // no depende de esto — el POS (carrito/caja) se vende como algo aparte, y el
    // negocio lo prende desde Configuración cuando lo necesite. Ver [[project]].
    private Boolean posHabilitado;
}
