package com.pos_backend.inventario.application.config;

import com.pos_backend.inventario.domain.model.gateway.CategoriaConsultaGateway;
import com.pos_backend.inventario.domain.model.gateway.ContabilidadGateway;
import com.pos_backend.inventario.domain.model.gateway.LectorExcelGateway;
import com.pos_backend.inventario.domain.model.gateway.MovimientoInventarioGateway;
import com.pos_backend.inventario.domain.model.gateway.ProductoGateway;
import com.pos_backend.inventario.domain.model.gateway.ProveedorConsultaGateway;
import com.pos_backend.inventario.domain.usecase.ProductoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProductoUseCase productoUseCase(
            ProductoGateway productoGateway,
            MovimientoInventarioGateway movimientoGateway,
            LectorExcelGateway lectorExcelGateway,
            CategoriaConsultaGateway categoriaConsultaGateway,
            ProveedorConsultaGateway proveedorConsultaGateway,
            ContabilidadGateway contabilidadGateway
    ) {
        return new ProductoUseCase(productoGateway, movimientoGateway, lectorExcelGateway,
                categoriaConsultaGateway, proveedorConsultaGateway, contabilidadGateway);
    }
}
