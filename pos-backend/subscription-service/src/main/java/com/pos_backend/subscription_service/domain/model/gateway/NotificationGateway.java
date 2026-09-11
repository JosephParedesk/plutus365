package com.pos_backend.subscription_service.domain.model.gateway;

import com.pos_backend.subscription_service.domain.model.Suscripcion;

public interface NotificationGateway {
    void enviarConfirmacionSuscripcion(Suscripcion suscripcion);
}