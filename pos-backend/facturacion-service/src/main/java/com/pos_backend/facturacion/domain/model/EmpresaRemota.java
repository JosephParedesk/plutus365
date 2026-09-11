package com.pos_backend.facturacion.domain.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmpresaRemota {
    private String empresaId;
    private String tipoPersona;
    private String tipoDocumento;
    private String numeroDocumento;
    private String dv;
    private String regimenFiscal;
    private String razonSocial;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String departamento;
    private String pais;
    private String logoUrl;
    private String colorPrincipal;
}
