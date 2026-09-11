package com.pos_backend.subscription_service.application.config;


import com.pos_backend.subscription_service.domain.model.gateway.NotificationGateway;
import com.pos_backend.subscription_service.domain.model.gateway.PlanGateway;
import com.pos_backend.subscription_service.domain.model.gateway.SuscripcionGateway;
import com.pos_backend.subscription_service.domain.usecase.PlanUseCase;
import com.pos_backend.subscription_service.domain.usecase.SuscripcionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class UseCaseConfig {

    @Bean
    public PlanUseCase planUseCase(PlanGateway planGateway) {
        return new PlanUseCase(planGateway);
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    public SuscripcionUseCase suscripcionUseCase(
            SuscripcionGateway suscripcionGateway,
            PlanGateway planGateway,
            NotificationGateway notificationGateway) {
        return new SuscripcionUseCase(suscripcionGateway, planGateway, notificationGateway);
    }
}