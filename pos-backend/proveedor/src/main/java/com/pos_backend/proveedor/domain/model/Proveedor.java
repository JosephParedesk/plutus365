package com.pos_backend.proveedor.domain.model;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Proveedor {
    private Long proveedorId;
    private String nit;
    private String nombre;
    private String contacto;
    private String telefono;
    private String correo;
    private String direccion;
    private String ciudad;
    private String plazoCredito;
    private Boolean activo;

    // null = proveedor base compartido (legado). No-null = privado de esa empresa.
    private String empresaId;
}