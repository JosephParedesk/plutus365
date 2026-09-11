package com.pos_backend.subscription_service.domain.usecase;

import com.pos_backend.subscription_service.domain.model.Plan;
import com.pos_backend.subscription_service.domain.model.Suscripcion;
import com.pos_backend.subscription_service.domain.model.gateway.NotificationGateway;
import com.pos_backend.subscription_service.domain.model.gateway.PlanGateway;
import com.pos_backend.subscription_service.domain.model.gateway.SuscripcionGateway;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class SuscripcionUseCase {

    private final SuscripcionGateway suscripcionGateway;
    private final PlanGateway planGateway;
    private final NotificationGateway notificationGateway;

    public Suscripcion crearSuscripcion(Suscripcion suscripcion) {
        if (suscripcion.getUsuarioCedula() == null || suscripcion.getUsuarioCedula().trim().isEmpty()) {
            throw new RuntimeException("La cédula del usuario es obligatoria");
        }
        if (suscripcion.getPlanId() == null) {
            throw new RuntimeException("El plan es obligatorio");
        }

        Plan plan = planGateway.buscarPlanPorId(suscripcion.getPlanId());
        if (plan == null) {
            throw new NoSuchElementException("Plan no encontrado");
        }

        suscripcion.setEstado("PENDIENTE");
        suscripcion.setFechaCreacion(LocalDateTime.now());
        suscripcion.setFechaInicio(LocalDateTime.now());
        suscripcion.setFechaFin(LocalDateTime.now().plusDays(14)); // 14 días gratis

        Suscripcion guardada = suscripcionGateway.guardarSuscripcion(suscripcion);
        notificationGateway.enviarConfirmacionSuscripcion(guardada);

        return guardada;
    }

    public Suscripcion activarSuscripcion(Long id) {
        Suscripcion suscripcion = suscripcionGateway.buscarPorId(id);
        if (suscripcion == null) {
            throw new NoSuchElementException("Suscripción no encontrada");
        }
        suscripcion.setEstado("ACTIVA");
        suscripcion.setFechaInicio(LocalDateTime.now());
        suscripcion.setFechaFin(LocalDateTime.now().plusMonths(1));
        return suscripcionGateway.guardarSuscripcion(suscripcion);
    }

    public Suscripcion buscarPorUsuario(String cedula) {
        Suscripcion suscripcion = suscripcionGateway.buscarPorUsuario(cedula);
        if (suscripcion == null) {
            throw new NoSuchElementException("Suscripción no encontrada para este usuario");
        }
        return suscripcion;
    }

    public void cancelarSuscripcion(Long id) {
        Suscripcion suscripcion = suscripcionGateway.buscarPorId(id);
        if (suscripcion == null) {
            throw new NoSuchElementException("Suscripción no encontrada");
        }
        suscripcionGateway.cancelarSuscripcion(id);
    }
}