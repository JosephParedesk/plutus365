package com.pos_backend.venta.infraestructure.driver_adapters.local_client;

import com.pos_backend.empresa.domain.model.Empresa;
import com.pos_backend.empresa.domain.usecase.EmpresaUseCase;
import com.pos_backend.venta.domain.model.EmpresaRemota;
import com.pos_backend.venta.domain.model.gateway.EmpresaConsultaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

// Reemplaza el http_client original (RestClient a empresa-service): ahora que
// empresa-service ya está migrado, es una llamada directa al UseCase. El
// original atrapaba un 404 HTTP y devolvía null si la empresa no había
// configurado sus datos todavía; localmente el UseCase lanza
// NoSuchElementException para ese mismo caso — se traduce a null igual.
@Component("ventaEmpresaConsultaGatewayImpl")
@RequiredArgsConstructor
public class EmpresaConsultaGatewayImpl implements EmpresaConsultaGateway {

    private final EmpresaUseCase empresaUseCase;

    @Override
    public EmpresaRemota buscarEmpresa(String empresaId) {
        Empresa empresa;
        try {
            empresa = empresaUseCase.obtenerConfiguracion(empresaId);
        } catch (NoSuchElementException e) {
            return null;
        }
        return new EmpresaRemota(
                empresa.getEmpresaId(), empresa.getTipoPersona(), empresa.getRazonSocial(),
                empresa.getNombres(), empresa.getApellidos(), empresa.getNombreComercial(),
                empresa.getLogoUrl(), empresa.getColorPrincipal());
    }
}
