package com.pos_backend.facturacion.domain.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProveedorRemota {
    private Long proveedorId;
    private String nit;
    private String nombre;
    private String telefono;
    private String correo;
    private String direccion;
    private String ciudad;
}
