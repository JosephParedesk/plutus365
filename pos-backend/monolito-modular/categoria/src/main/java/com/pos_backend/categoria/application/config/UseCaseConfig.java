package com.pos_backend.categoria.application.config;

import com.pos_backend.categoria.domain.model.gateway.CategoriaGateway;
import com.pos_backend.categoria.domain.usecase.CategoriaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CategoriaUseCase categoriaUseCase(CategoriaGateway categoriaGateway) {
        return new CategoriaUseCase(categoriaGateway);
    }
}