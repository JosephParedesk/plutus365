package com.pos_backend.facturacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Fila de usuarios del panel de super administrador — nunca incluye la
// contraseña (ni siquiera cifrada): no hace falta y es un dato sensible que
// no debe salir del backend en una respuesta HTTP.
@Getter
@AllArgsConstructor
public class UsuarioAdminResumen {
    private String cedula;
    private String nombre;
    private String correo;
    private String rol;
    private String empresaId;
}
