package com.pos_backend.auth.domain.model.gateway;

import com.pos_backend.auth.domain.model.Usuario;

public interface NotificationGateway {
    void enviarNotificacion(Usuario usuario);
    void enviarNotificacionRecuperacion(Usuario usuario);
}