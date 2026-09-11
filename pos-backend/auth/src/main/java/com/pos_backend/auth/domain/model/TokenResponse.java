package com.pos_backend.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Lo que devuelve login()/refrescarToken(): un access token corto (JWT, 1h) y un
// refreshToken de sesión (rota en cada refresh) para renovarlo sin pedir contraseña.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private LocalDateTime refreshExpiraEn;
}
