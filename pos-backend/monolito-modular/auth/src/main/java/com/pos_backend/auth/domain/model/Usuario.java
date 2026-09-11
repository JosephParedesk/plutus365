
package com.pos_backend.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Usuario {
    private String cedula;
    private String tipoDocumento;
    private String nombre;
    private String correo;
    private String contrasena;
    private String telefono;
    private String rol;
    private Long planId;
    private String empresaId;
    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpiry;

    // Sesión — el access token (JWT) dura 1h fijo (JwtGatewayImpl); mientras el
    // refreshToken siga vigente, /refresh emite un JWT nuevo sin pedir contraseña
    // otra vez. Se rota en cada refresh (sliding session). recordarme decide la
    // ventana: 30 días si se marcó en el login, 1 día si no.
    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;
    private Boolean refreshTokenRecordarme;
}
