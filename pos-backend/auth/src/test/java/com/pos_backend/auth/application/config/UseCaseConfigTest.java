package com.pos_backend.auth.application.config;

import com.pos_backend.auth.domain.model.gateway.EncrypterGateway;
import com.pos_backend.auth.domain.model.gateway.JwtGateway;
import com.pos_backend.auth.domain.model.gateway.NotificationGateway;
import com.pos_backend.auth.domain.model.gateway.UsuarioGateway;
import com.pos_backend.auth.domain.usecase.UsuarioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UseCaseConfigTest {

    private UseCaseConfig useCaseConfig;

    @BeforeEach
    void setUp() {
        useCaseConfig = new UseCaseConfig();
    }

    @Test
    void usuarioUseCase_Exitoso_RetornaInstancia() {
        // Arrange
        UsuarioGateway usuarioGateway = mock(UsuarioGateway.class);
        EncrypterGateway encrypterGateway = mock(EncrypterGateway.class);
        JwtGateway jwtGateway = mock(JwtGateway.class);
        NotificationGateway notificationGateway = mock(NotificationGateway.class);

        // Act
        UsuarioUseCase resultado = useCaseConfig.usuarioUseCase(usuarioGateway, encrypterGateway, jwtGateway, notificationGateway);

        // Assert
        assertNotNull(resultado);
    }

    @Test
    void restTemplate_Exitoso_RetornaInstancia() {
        // Arrange

        // Act
        RestTemplate resultado = useCaseConfig.restTemplate();

        // Assert
        assertNotNull(resultado);
    }
}