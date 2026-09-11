package com.pos_backend.cliente.application.config;

import com.pos_backend.cliente.domain.model.gateway.ClienteGateway;
import com.pos_backend.cliente.domain.usecase.ClienteUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ClienteUseCase clienteUseCase(ClienteGateway clienteGateway) {
        return new ClienteUseCase(clienteGateway);
    }
}
