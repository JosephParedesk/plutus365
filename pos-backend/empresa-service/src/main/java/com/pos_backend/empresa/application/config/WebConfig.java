package com.pos_backend.empresa.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/logos}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String ubicacion = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        registry.addResourceHandler("/uploads/logos/**")
                .addResourceLocations("file:" + ubicacion);
    }
}
