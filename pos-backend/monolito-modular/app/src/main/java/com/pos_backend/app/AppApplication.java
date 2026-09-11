package com.pos_backend.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// scanBasePackages/EntityScan/EnableJpaRepositories apuntan a la raíz común
// com.pos_backend en vez de solo com.pos_backend.app: cada módulo de negocio
// migrado (categoria, proveedor, ...) vive en su propio paquete
// com.pos_backend.<modulo>, y Spring Boot por defecto solo escanea el paquete
// del main class. Sin esto, ningún @RestController/@Repository/@Entity de los
// módulos se registraría.
@SpringBootApplication(scanBasePackages = "com.pos_backend")
@EntityScan(basePackages = "com.pos_backend")
@EnableJpaRepositories(basePackages = "com.pos_backend")
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

}
