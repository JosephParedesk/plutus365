package com.pos_backend.auth.application.config;

import com.pos_backend.auth.domain.model.gateway.EncrypterGateway;
import com.pos_backend.auth.domain.model.gateway.JwtGateway;
import com.pos_backend.auth.domain.model.gateway.NotificationGateway;
import com.pos_backend.auth.domain.model.gateway.UsuarioGateway;
import com.pos_backend.auth.domain.usecase.UsuarioUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    public UsuarioUseCase usuarioUseCase(UsuarioGateway usuarioGateway, EncrypterGateway encrypterGateway, JwtGateway jwtGateway, NotificationGateway notificationGateway) {
        return new UsuarioUseCase(usuarioGateway, encrypterGateway, jwtGateway, notificationGateway);
    }
}
