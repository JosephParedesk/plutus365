package com.pos_backend.empresa.application.config;

import com.pos_backend.empresa.domain.model.gateway.ContabilidadGateway;
import com.pos_backend.empresa.domain.model.gateway.EmpresaGateway;
import com.pos_backend.empresa.domain.usecase.EmpresaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Nombre de bean explícito: ver el comentario en
// categoria/application/config/UseCaseConfig.java.
@Configuration("empresaUseCaseConfig")
public class UseCaseConfig {

    @Bean
    public EmpresaUseCase empresaUseCase(EmpresaGateway empresaGateway, ContabilidadGateway contabilidadGateway) {
        return new EmpresaUseCase(empresaGateway, contabilidadGateway);
    }
}
