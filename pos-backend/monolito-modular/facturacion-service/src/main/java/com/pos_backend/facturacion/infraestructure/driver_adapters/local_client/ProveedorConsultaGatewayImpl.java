package com.pos_backend.facturacion.infraestructure.driver_adapters.local_client;

import com.pos_backend.facturacion.domain.model.ProveedorRemota;
import com.pos_backend.facturacion.domain.model.gateway.ProveedorConsultaGateway;
import com.pos_backend.proveedor.domain.model.Proveedor;
import com.pos_backend.proveedor.domain.usecase.ProveedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

// Reemplaza el http_client original (RestClient a proveedor-service): ahora
// que proveedor ya está migrado, es una llamada directa al UseCase.
@Component("facturacionProveedorConsultaGatewayImpl")
@RequiredArgsConstructor
public class ProveedorConsultaGatewayImpl implements ProveedorConsultaGateway {

    private final ProveedorUseCase proveedorUseCase;

    @Override
    public ProveedorRemota buscarProveedor(Long proveedorId, String empresaId) {
        Proveedor p;
        try {
            p = proveedorUseCase.buscarProveedorPorId(proveedorId, empresaId);
        } catch (NoSuchElementException e) {
            return null;
        }
        return new ProveedorRemota(
                p.getProveedorId(), p.getNit(), p.getNombre(), p.getTelefono(), p.getCorreo(),
                p.getDireccion(), p.getCiudad());
    }
}
