package com.pos_backend.categoria.application.config;

import com.pos_backend.categoria.domain.model.gateway.CategoriaGateway;
import com.pos_backend.categoria.domain.usecase.CategoriaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Nombre de bean explícito: dos módulos con una clase UseCaseConfig homónima
// (mismo simple name, distinto paquete) generan el mismo id de bean por
// default y Spring tira ConflictingBeanDefinitionException al convivir en un
// solo contexto — se detectó al migrar proveedor junto a categoria.
@Configuration("categoriaUseCaseConfig")
public class UseCaseConfig {

    @Bean
    public CategoriaUseCase categoriaUseCase(CategoriaGateway categoriaGateway) {
        return new CategoriaUseCase(categoriaGateway);
    }
}