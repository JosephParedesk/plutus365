package com.pos_backend.cliente.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Cliente {
    private Long clienteId;

    // Clasificación DIAN
    private String tipoPersona;      // NATURAL | JURIDICA
    private String tipoDocumento;    // CC, NIT, CE, PASAPORTE, TI, RC
    private String numeroDocumento;
    private String dv;               // Dígito de verificación (solo NIT)
    private String regimenFiscal;    // RESPONSABLE_IVA | NO_RESPONSABLE_IVA

    // Identificación / nombre
    private String nombres;          // Persona natural
    private String apellidos;        // Persona natural
    private String razonSocial;      // Persona jurídica

    // Contacto
    private String correo;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String departamento;
    private String pais;
    private String codigoPostal;

    private Boolean activo;
    private String empresaId;
}
