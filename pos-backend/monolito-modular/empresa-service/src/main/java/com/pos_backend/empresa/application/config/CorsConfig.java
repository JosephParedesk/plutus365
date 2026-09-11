package com.pos_backend.empresa.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

// El logo (/uploads/logos/**) se sirve directo desde este servicio, sin pasar por
// el gateway (ver WebConfig) — un <img src> lo muestra igual sin CORS, pero leerlo
// con fetch() (para incrustarlo en un Excel, por ejemplo) sí lo necesita. Mismos
// orígenes que CorsConfig.java del gateway.
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "https://*.vercel.app"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/uploads/logos/**", config);

        return new CorsFilter(source);
    }
}
