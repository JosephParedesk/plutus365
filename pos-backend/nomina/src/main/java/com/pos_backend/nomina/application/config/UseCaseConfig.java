package com.pos_backend.nomina.application.config;

import com.pos_backend.nomina.domain.model.gateway.AcumuladoInicialGateway;
import com.pos_backend.nomina.domain.model.gateway.EmpleadoGateway;
import com.pos_backend.nomina.domain.model.gateway.NominaGateway;
import com.pos_backend.nomina.domain.usecase.AcumuladoInicialUseCase;
import com.pos_backend.nomina.domain.usecase.EmpleadoUseCase;
import com.pos_backend.nomina.domain.usecase.NominaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public EmpleadoUseCase empleadoUseCase(EmpleadoGateway empleadoGateway) {
        return new EmpleadoUseCase(empleadoGateway);
    }

    @Bean
    public NominaUseCase nominaUseCase(NominaGateway nominaGateway, EmpleadoGateway empleadoGateway) {
        return new NominaUseCase(nominaGateway, empleadoGateway);
    }

    @Bean
    public AcumuladoInicialUseCase acumuladoInicialUseCase(
            AcumuladoInicialGateway acumuladoInicialGateway, EmpleadoGateway empleadoGateway) {
        return new AcumuladoInicialUseCase(acumuladoInicialGateway, empleadoGateway);
    }
}
