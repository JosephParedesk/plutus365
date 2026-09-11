package com.pos_backend.contabilidad.application.config;

import com.pos_backend.contabilidad.domain.model.gateway.AsientoContableGateway;
import com.pos_backend.contabilidad.domain.model.gateway.CentroCostoGateway;
import com.pos_backend.contabilidad.domain.model.gateway.CuentaContableGateway;
import com.pos_backend.contabilidad.domain.model.gateway.PucBaseGateway;
import com.pos_backend.contabilidad.domain.usecase.AsientoContableUseCase;
import com.pos_backend.contabilidad.domain.usecase.CentroCostoUseCase;
import com.pos_backend.contabilidad.domain.usecase.CuentaContableUseCase;
import com.pos_backend.contabilidad.domain.usecase.EstadosFinancierosUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Nombre de bean explícito: ver el comentario en
// categoria/application/config/UseCaseConfig.java.
@Configuration("contabilidadUseCaseConfig")
public class UseCaseConfig {

    @Bean
    public CuentaContableUseCase cuentaContableUseCase(
            CuentaContableGateway cuentaContableGateway,
            PucBaseGateway pucBaseGateway
    ) {
        return new CuentaContableUseCase(cuentaContableGateway, pucBaseGateway);
    }

    @Bean
    public AsientoContableUseCase asientoContableUseCase(
            AsientoContableGateway asientoContableGateway,
            CuentaContableGateway cuentaContableGateway
    ) {
        return new AsientoContableUseCase(asientoContableGateway, cuentaContableGateway);
    }

    @Bean
    public EstadosFinancierosUseCase estadosFinancierosUseCase(
            AsientoContableGateway asientoContableGateway,
            CuentaContableGateway cuentaContableGateway
    ) {
        return new EstadosFinancierosUseCase(asientoContableGateway, cuentaContableGateway);
    }

    @Bean
    public CentroCostoUseCase centroCostoUseCase(CentroCostoGateway centroCostoGateway) {
        return new CentroCostoUseCase(centroCostoGateway);
    }
}
