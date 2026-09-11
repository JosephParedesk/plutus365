package com.pos_backend.venta.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

// Solo los campos que necesita el comprobante de venta por correo — no es una
// copia completa de Empresa.java (eso viviría en empresa-service). Ignora el
// resto de campos que trae la respuesta real (tipoDocumento, correo, etc.).
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmpresaRemota {
    private String empresaId;
    private String tipoPersona;
    private String razonSocial;
    private String nombres;
    private String apellidos;
    private String nombreComercial;
    private String logoUrl;
    private String colorPrincipal;
}
