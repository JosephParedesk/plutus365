package com.pos_backend.facturacion.application.config;

import com.pos_backend.facturacion.domain.model.gateway.*;
import com.pos_backend.facturacion.domain.usecase.ConfiguracionDianUseCase;
import com.pos_backend.facturacion.domain.usecase.FacturaUseCase;
import com.pos_backend.facturacion.domain.usecase.NotaCreditoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ConfiguracionDianUseCase configuracionDianUseCase(
            ConfiguracionDianGateway configuracionDianGateway,
            CertificadoGateway certificadoGateway
    ) {
        return new ConfiguracionDianUseCase(configuracionDianGateway, certificadoGateway);
    }

    @Bean
    public FacturaUseCase facturaUseCase(
            FacturaGateway facturaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            CertificadoGateway certificadoGateway,
            VentaConsultaGateway ventaConsultaGateway,
            ClienteConsultaGateway clienteConsultaGateway,
            EmpresaConsultaGateway empresaConsultaGateway,
            XmlFacturaGateway xmlFacturaGateway,
            DianGateway dianGateway,
            QrCodeGateway qrCodeGateway,
            EmailGateway emailGateway
    ) {
        return new FacturaUseCase(
                facturaGateway, configuracionDianGateway, certificadoGateway,
                ventaConsultaGateway, clienteConsultaGateway, empresaConsultaGateway,
                xmlFacturaGateway, dianGateway, qrCodeGateway, emailGateway
        );
    }

    @Bean
    public NotaCreditoUseCase notaCreditoUseCase(
            NotaCreditoGateway notaCreditoGateway,
            FacturaGateway facturaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            StockNotaGateway stockNotaGateway,
            ContabilidadNotaGateway contabilidadNotaGateway
    ) {
        return new NotaCreditoUseCase(notaCreditoGateway, facturaGateway, configuracionDianGateway,
                stockNotaGateway, contabilidadNotaGateway);
    }
}
