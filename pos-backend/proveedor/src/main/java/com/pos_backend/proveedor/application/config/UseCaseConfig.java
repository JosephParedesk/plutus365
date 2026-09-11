package com.pos_backend.proveedor.application.config;

import com.pos_backend.proveedor.domain.model.gateway.ProveedorGateway;
import com.pos_backend.proveedor.domain.usecase.ProveedorUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProveedorUseCase proveedorUseCase(ProveedorGateway proveedorGateway) {
        return new ProveedorUseCase(proveedorGateway);
    }
}