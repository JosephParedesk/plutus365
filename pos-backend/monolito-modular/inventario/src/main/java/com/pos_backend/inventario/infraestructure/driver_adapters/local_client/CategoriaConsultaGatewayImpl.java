package com.pos_backend.inventario.infraestructure.driver_adapters.local_client;

import com.pos_backend.categoria.domain.model.Categoria;
import com.pos_backend.categoria.domain.usecase.CategoriaUseCase;
import com.pos_backend.inventario.domain.model.gateway.CategoriaConsultaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Reemplaza el http_client original (RestClient a categoria-service): ahora que
// categoria-service ya está migrado, es una llamada directa al UseCase en vez de
// una llamada de red. Mismo comportamiento: busca por nombre exacto
// (case-insensitive), null si no hay match o si el usecase falla — nunca debe
// tumbar la importación del catálogo por esto.
@Component("inventarioCategoriaConsultaGatewayImpl")
@RequiredArgsConstructor
public class CategoriaConsultaGatewayImpl implements CategoriaConsultaGateway {

    private final CategoriaUseCase categoriaUseCase;

    @Override
    public String buscarIdPorNombre(String nombre, String empresaId) {
        if (nombre == null || nombre.isBlank()) return null;
        try {
            for (Categoria c : categoriaUseCase.listarCategorias(empresaId))
                if (c.getNombre() != null && nombre.trim().equalsIgnoreCase(c.getNombre().trim()))
                    return String.valueOf(c.getCategoriaId());
        } catch (Exception ignored) {
            // si la consulta falla, la fila queda sin categoría y sigue el resto de la importación
        }
        return null;
    }
}
