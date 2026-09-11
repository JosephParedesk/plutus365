package com.pos_backend.inventario.infraestructure.driver_adapters.local_client;

import com.pos_backend.inventario.domain.model.gateway.ProveedorConsultaGateway;
import com.pos_backend.proveedor.domain.model.Proveedor;
import com.pos_backend.proveedor.domain.usecase.ProveedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Reemplaza el http_client original (RestClient a proveedor-service): ver el
// mismo comentario en CategoriaConsultaGatewayImpl de este mismo paquete.
@Component("inventarioProveedorConsultaGatewayImpl")
@RequiredArgsConstructor
public class ProveedorConsultaGatewayImpl implements ProveedorConsultaGateway {

    private final ProveedorUseCase proveedorUseCase;

    @Override
    public String buscarIdPorNombre(String nombre, String empresaId) {
        if (nombre == null || nombre.isBlank()) return null;
        try {
            for (Proveedor p : proveedorUseCase.listarProveedores(empresaId))
                if (p.getNombre() != null && nombre.trim().equalsIgnoreCase(p.getNombre().trim()))
                    return String.valueOf(p.getProveedorId());
        } catch (Exception ignored) {
            // si la consulta falla, la fila queda sin proveedor y sigue el resto de la importación
        }
        return null;
    }
}
