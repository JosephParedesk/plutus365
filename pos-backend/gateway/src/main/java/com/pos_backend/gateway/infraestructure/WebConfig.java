package com.pos_backend.gateway.infraestructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final PermisosInterceptor permisosInterceptor;

    public WebConfig(PermisosInterceptor permisosInterceptor) {
        this.permisosInterceptor = permisosInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // empresaId ya no se registra acá: EmpresaIdInterceptor es un Filter
        // (Spring Boot lo registra solo), no un HandlerInterceptor. Tiene que
        // serlo para poder agregar el header X-Empresa-Id al request antes de
        // que Spring Cloud Gateway MVC lo capture para reenviarlo.
        registry.addInterceptor(permisosInterceptor);
    }
}