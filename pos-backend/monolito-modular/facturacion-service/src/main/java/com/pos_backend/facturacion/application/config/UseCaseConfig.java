package com.pos_backend.facturacion.application.config;

import com.pos_backend.facturacion.domain.model.gateway.*;
import com.pos_backend.facturacion.domain.usecase.AdminUseCase;
import com.pos_backend.facturacion.domain.usecase.ConfiguracionDianUseCase;
import com.pos_backend.facturacion.domain.usecase.FacturaUseCase;
import com.pos_backend.facturacion.domain.usecase.NotaCreditoUseCase;
import com.pos_backend.facturacion.domain.usecase.NotaDebitoUseCase;
import com.pos_backend.facturacion.domain.usecase.DocumentoSoporteUseCase;
import com.pos_backend.facturacion.domain.usecase.NominaElectronicaUseCase;
import com.pos_backend.facturacion.domain.usecase.NotaAjusteDocumentoSoporteUseCase;
import com.pos_backend.facturacion.domain.usecase.NotaAjusteNominaUseCase;
import com.pos_backend.facturacion.domain.usecase.RecepcionDocumentoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Nombre de bean explícito: ver el comentario en
// categoria/application/config/UseCaseConfig.java.
@Configuration("facturacionUseCaseConfig")
public class UseCaseConfig {

    @Bean
    public ConfiguracionDianUseCase configuracionDianUseCase(
            ConfiguracionDianGateway configuracionDianGateway,
            FacturaElectronicaGateway facturaElectronicaGateway
    ) {
        return new ConfiguracionDianUseCase(configuracionDianGateway, facturaElectronicaGateway);
    }

    @Bean
    public AdminUseCase adminUseCase(
            EmpresaConsultaGateway empresaConsultaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            FacturaGateway facturaGateway,
            UsuarioConsultaGateway usuarioConsultaGateway,
            FacturaElectronicaGateway facturaElectronicaGateway
    ) {
        return new AdminUseCase(empresaConsultaGateway, configuracionDianGateway, facturaGateway, usuarioConsultaGateway, facturaElectronicaGateway);
    }

    @Bean
    public FacturaUseCase facturaUseCase(
            FacturaGateway facturaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            VentaConsultaGateway ventaConsultaGateway,
            ClienteConsultaGateway clienteConsultaGateway,
            EmpresaConsultaGateway empresaConsultaGateway,
            FacturaElectronicaGateway facturaElectronicaGateway,
            QrCodeGateway qrCodeGateway,
            EmailGateway emailGateway
    ) {
        return new FacturaUseCase(
                facturaGateway, configuracionDianGateway,
                ventaConsultaGateway, clienteConsultaGateway, empresaConsultaGateway,
                facturaElectronicaGateway, qrCodeGateway, emailGateway
        );
    }

    @Bean
    public NotaCreditoUseCase notaCreditoUseCase(
            NotaCreditoGateway notaCreditoGateway,
            FacturaGateway facturaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            VentaConsultaGateway ventaConsultaGateway,
            ClienteConsultaGateway clienteConsultaGateway,
            EmpresaConsultaGateway empresaConsultaGateway,
            FacturaElectronicaGateway facturaElectronicaGateway,
            StockNotaGateway stockNotaGateway,
            ContabilidadNotaGateway contabilidadNotaGateway
    ) {
        return new NotaCreditoUseCase(notaCreditoGateway, facturaGateway, configuracionDianGateway,
                ventaConsultaGateway, clienteConsultaGateway, empresaConsultaGateway, facturaElectronicaGateway,
                stockNotaGateway, contabilidadNotaGateway);
    }

    @Bean
    public NotaDebitoUseCase notaDebitoUseCase(
            NotaDebitoGateway notaDebitoGateway,
            FacturaGateway facturaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            FacturaElectronicaGateway facturaElectronicaGateway,
            VentaConsultaGateway ventaConsultaGateway,
            ClienteConsultaGateway clienteConsultaGateway,
            EmpresaConsultaGateway empresaConsultaGateway
    ) {
        return new NotaDebitoUseCase(notaDebitoGateway, facturaGateway, configuracionDianGateway, facturaElectronicaGateway,
                ventaConsultaGateway, clienteConsultaGateway, empresaConsultaGateway);
    }

    @Bean
    public DocumentoSoporteUseCase documentoSoporteUseCase(
            DocumentoSoporteGateway documentoSoporteGateway,
            CompraConsultaGateway compraConsultaGateway,
            ProveedorConsultaGateway proveedorConsultaGateway,
            EmpresaConsultaGateway empresaConsultaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            FacturaElectronicaGateway facturaElectronicaGateway
    ) {
        return new DocumentoSoporteUseCase(documentoSoporteGateway, compraConsultaGateway, proveedorConsultaGateway,
                empresaConsultaGateway, configuracionDianGateway, facturaElectronicaGateway);
    }

    @Bean
    public NominaElectronicaUseCase nominaElectronicaUseCase(
            NominaElectronicaGateway nominaElectronicaGateway,
            NominaConsultaGateway nominaConsultaGateway,
            EmpleadoConsultaGateway empleadoConsultaGateway,
            EmpresaConsultaGateway empresaConsultaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            FacturaElectronicaGateway facturaElectronicaGateway
    ) {
        return new NominaElectronicaUseCase(nominaElectronicaGateway, nominaConsultaGateway, empleadoConsultaGateway,
                empresaConsultaGateway, configuracionDianGateway, facturaElectronicaGateway);
    }

    @Bean
    public NotaAjusteDocumentoSoporteUseCase notaAjusteDocumentoSoporteUseCase(
            NotaAjusteDocumentoSoporteGateway notaAjusteDocumentoSoporteGateway,
            DocumentoSoporteGateway documentoSoporteGateway,
            ProveedorConsultaGateway proveedorConsultaGateway,
            EmpresaConsultaGateway empresaConsultaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            FacturaElectronicaGateway facturaElectronicaGateway
    ) {
        return new NotaAjusteDocumentoSoporteUseCase(notaAjusteDocumentoSoporteGateway, documentoSoporteGateway,
                proveedorConsultaGateway, empresaConsultaGateway, configuracionDianGateway, facturaElectronicaGateway);
    }

    @Bean
    public NotaAjusteNominaUseCase notaAjusteNominaUseCase(
            NotaAjusteNominaGateway notaAjusteNominaGateway,
            NominaElectronicaGateway nominaElectronicaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            FacturaElectronicaGateway facturaElectronicaGateway
    ) {
        return new NotaAjusteNominaUseCase(notaAjusteNominaGateway, nominaElectronicaGateway,
                configuracionDianGateway, facturaElectronicaGateway);
    }

    @Bean
    public RecepcionDocumentoUseCase recepcionDocumentoUseCase(
            RecepcionDocumentoGateway recepcionDocumentoGateway,
            CompraConsultaGateway compraConsultaGateway,
            ConfiguracionDianGateway configuracionDianGateway,
            FacturaElectronicaGateway facturaElectronicaGateway
    ) {
        return new RecepcionDocumentoUseCase(recepcionDocumentoGateway, compraConsultaGateway,
                configuracionDianGateway, facturaElectronicaGateway);
    }
}
