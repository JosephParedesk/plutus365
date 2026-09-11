package com.pos_backend.subscription_service.infraestructure.notification;

import com.pos_backend.subscription_service.domain.model.Suscripcion;
import com.pos_backend.subscription_service.domain.model.gateway.NotificationGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationGatewayImpl implements NotificationGateway {

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    private final RestTemplate restTemplate;

    @Override
    public void enviarConfirmacionSuscripcion(Suscripcion suscripcion) {
        try {
            Map<String, Object> payload = Map.of(
                    "usuarioCedula", suscripcion.getUsuarioCedula(),
                    "planId", suscripcion.getPlanId(),
                    "estado", suscripcion.getEstado(),
                    "fechaFin", suscripcion.getFechaFin().toString()
            );
            restTemplate.postForObject(notificationServiceUrl, payload, String.class);
        } catch (Exception e) {
            System.out.println("Error al enviar notificación: " + e.getMessage());
        }
    }
}
