package com.pos_backend.compra.application.config;

import com.pos_backend.compra.domain.model.gateway.CompraGateway;
import com.pos_backend.compra.domain.model.gateway.ContabilidadGateway;
import com.pos_backend.compra.domain.model.gateway.ImportadorFacturaGateway;
import com.pos_backend.compra.domain.model.gateway.StockGateway;
import com.pos_backend.compra.domain.usecase.CompraUseCase;
import com.pos_backend.compra.domain.usecase.ImportarFacturaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Nombre de bean explícito: ver el comentario en
// categoria/application/config/UseCaseConfig.java.
@Configuration("compraUseCaseConfig")
public class UseCaseConfig {

    @Bean
    public CompraUseCase compraUseCase(
            CompraGateway compraGateway, ContabilidadGateway contabilidadGateway, StockGateway stockGateway
    ) {
        return new CompraUseCase(compraGateway, contabilidadGateway, stockGateway);
    }

    @Bean
    public ImportarFacturaUseCase importarFacturaUseCase(ImportadorFacturaGateway importadorFacturaGateway) {
        return new ImportarFacturaUseCase(importadorFacturaGateway);
    }
}