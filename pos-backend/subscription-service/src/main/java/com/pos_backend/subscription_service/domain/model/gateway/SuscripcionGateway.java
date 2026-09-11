package com.pos_backend.subscription_service.domain.model.gateway;


import com.pos_backend.subscription_service.domain.model.Suscripcion;

import java.util.List;

public interface SuscripcionGateway {
    Suscripcion guardarSuscripcion(Suscripcion suscripcion);
    Suscripcion buscarPorUsuario(String usuarioCedula);
    Suscripcion buscarPorId(Long id);
    List<Suscripcion> listarTodas();
    void cancelarSuscripcion(Long id);
}