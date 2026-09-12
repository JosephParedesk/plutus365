package com.pos_backend.facturacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Fila de la tabla del panel de super administrador (ver AdminUseCase). Solo
// lo necesario para identificar la empresa y saber si ya tiene Factus activo
// — las credenciales en sí se piden aparte con GET /empresas/{id}/factus.
@Getter
@AllArgsConstructor
public class EmpresaAdminResumen {
    private String empresaId;
    private String nombre;
    private String tipoDocumento;
    private String numeroDocumento;
    private String correo;
    private boolean factusConfigurado;
    private int facturasEmitidas;
}
