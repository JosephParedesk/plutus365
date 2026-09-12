package com.pos_backend.facturacion.infraestructure.driver_adapters.local_client;

import com.pos_backend.empresa.domain.model.Empresa;
import com.pos_backend.empresa.domain.usecase.EmpresaUseCase;
import com.pos_backend.facturacion.domain.model.EmpresaRemota;
import com.pos_backend.facturacion.domain.model.gateway.EmpresaConsultaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

// Reemplaza el http_client original (RestClient a empresa-service): ver el
// mismo comentario en ClienteConsultaGatewayImpl de este mismo paquete.
@Component("facturacionEmpresaConsultaGatewayImpl")
@RequiredArgsConstructor
public class EmpresaConsultaGatewayImpl implements EmpresaConsultaGateway {

    private final EmpresaUseCase empresaUseCase;

    @Override
    public EmpresaRemota buscarEmpresa(String empresaId) {
        Empresa e;
        try {
            e = empresaUseCase.obtenerConfiguracion(empresaId);
        } catch (NoSuchElementException ex) {
            return null;
        }
        return aRemota(e);
    }

    @Override
    public java.util.List<EmpresaRemota> listarTodas() {
        return empresaUseCase.listarTodas().stream().map(this::aRemota).toList();
    }

    private EmpresaRemota aRemota(Empresa e) {
        return new EmpresaRemota(
                e.getEmpresaId(), e.getTipoPersona(), e.getTipoDocumento(), e.getNumeroDocumento(), e.getDv(),
                e.getRegimenFiscal(), e.getRazonSocial(), e.getNombres(), e.getApellidos(), e.getCorreo(),
                e.getTelefono(), e.getDireccion(), e.getCiudad(), e.getDepartamento(), e.getPais(),
                e.getLogoUrl(), e.getColorPrincipal());
    }
}
