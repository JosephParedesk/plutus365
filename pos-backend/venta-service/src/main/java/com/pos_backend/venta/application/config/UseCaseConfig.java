package com.pos_backend.venta.application.config;

import com.pos_backend.venta.domain.model.gateway.CajaGateway;
import com.pos_backend.venta.domain.model.gateway.ConfiguracionReciboGateway;
import com.pos_backend.venta.domain.model.gateway.CotizacionGateway;
import com.pos_backend.venta.domain.model.gateway.FacturaRecurrenteGateway;
import com.pos_backend.venta.domain.model.gateway.NotaDebitoVentaGateway;
import com.pos_backend.venta.domain.model.gateway.ReciboCajaGateway;
import com.pos_backend.venta.domain.model.gateway.ReciboContabilidadGateway;
import com.pos_backend.venta.domain.model.gateway.ContabilidadGateway;
import com.pos_backend.venta.domain.model.gateway.EmailGateway;
import com.pos_backend.venta.domain.model.gateway.EmpresaConsultaGateway;
import com.pos_backend.venta.domain.model.gateway.RemisionGateway;
import com.pos_backend.venta.domain.model.gateway.StockGateway;
import com.pos_backend.venta.domain.model.gateway.VentaGateway;
import com.pos_backend.venta.domain.usecase.CajaUseCase;
import com.pos_backend.venta.domain.usecase.ConfiguracionReciboUseCase;
import com.pos_backend.venta.domain.usecase.CotizacionUseCase;
import com.pos_backend.venta.domain.usecase.FacturaRecurrenteUseCase;
import com.pos_backend.venta.domain.usecase.NotaDebitoVentaUseCase;
import com.pos_backend.venta.domain.usecase.ReciboCajaUseCase;
import com.pos_backend.venta.domain.usecase.RemisionUseCase;
import com.pos_backend.venta.domain.usecase.VentaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public VentaUseCase ventaUseCase(
            VentaGateway ventaGateway, StockGateway stockGateway,
            EmailGateway emailGateway, ContabilidadGateway contabilidadGateway,
            EmpresaConsultaGateway empresaConsultaGateway
    ) {
        return new VentaUseCase(ventaGateway, stockGateway, emailGateway, contabilidadGateway, empresaConsultaGateway);
    }

    @Bean
    public CajaUseCase cajaUseCase(CajaGateway cajaGateway, VentaGateway ventaGateway) {
        return new CajaUseCase(cajaGateway, ventaGateway);
    }

    @Bean
    public CotizacionUseCase cotizacionUseCase(CotizacionGateway cotizacionGateway, VentaUseCase ventaUseCase) {
        return new CotizacionUseCase(cotizacionGateway, ventaUseCase);
    }

    @Bean
    public FacturaRecurrenteUseCase facturaRecurrenteUseCase(
            FacturaRecurrenteGateway recurrenteGateway, VentaUseCase ventaUseCase) {
        return new FacturaRecurrenteUseCase(recurrenteGateway, ventaUseCase);
    }

    @Bean
    public ReciboCajaUseCase reciboCajaUseCase(
            ReciboCajaGateway reciboCajaGateway, VentaGateway ventaGateway,
            ReciboContabilidadGateway reciboContabilidadGateway) {
        return new ReciboCajaUseCase(reciboCajaGateway, ventaGateway, reciboContabilidadGateway);
    }

    @Bean
    public RemisionUseCase remisionUseCase(RemisionGateway remisionGateway, VentaUseCase ventaUseCase) {
        return new RemisionUseCase(remisionGateway, ventaUseCase);
    }

    @Bean
    public NotaDebitoVentaUseCase notaDebitoVentaUseCase(
            NotaDebitoVentaGateway notaDebitoVentaGateway, VentaGateway ventaGateway) {
        return new NotaDebitoVentaUseCase(notaDebitoVentaGateway, ventaGateway);
    }

    @Bean
    public ConfiguracionReciboUseCase configuracionReciboUseCase(ConfiguracionReciboGateway configuracionReciboGateway) {
        return new ConfiguracionReciboUseCase(configuracionReciboGateway);
    }
}
